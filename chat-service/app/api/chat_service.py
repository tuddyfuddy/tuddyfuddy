import httpx
from langchain.memory import ConversationBufferMemory
from langchain.prompts import ChatPromptTemplate
from langchain_community.chat_message_histories import RedisChatMessageHistory

from app.api.kafka_service import KafkaService
from app.core.logger import setup_logger
from app.core.config import settings
from langchain_openai import ChatOpenAI
from langchain.schema.runnable import RunnablePassthrough

from app.models.templates import (
    ai_name,
    SYSTEM_MESSAGE_1,
    SYSTEM_MESSAGE_2,
    SYSTEM_MESSAGE_3,
    SYSTEM_MESSAGE_4,
    USER_MESSAGE_TEMPLATE,
    NATURAL_RESPONSE_TEMPLATE,
)


logging = setup_logger("app")


class ChatService:
    llm = ChatOpenAI(
        api_key=settings.GPT_KEY,
        model_name="gpt-4o-mini",
        temperature=0.8,
    )
    llm_f = ChatOpenAI(
        api_key=settings.FUDDY_KEY,
        model_name="ft:gpt-4o-mini-2024-07-18:personal::AUnUqTo6",
        temperature=0.8,
    )
    llm_t = ChatOpenAI(
        api_key=settings.TUDDY_KEY,
        model_name="ft:gpt-4o-mini-2024-07-18:personal::AUo5uob6",
        temperature=0.8,
    )

    ######################################

    @staticmethod
    async def process_chat(room_id: int, user_id: str, message: str):
        # 감정 분석
        emotion = await MessageProcessor.get_emotion(message)
        logging.info(f">>>>>>> ({emotion}) {message}")

        # LLM 설정
        message_length = len(message)
        max_response_length = MessageProcessor.calculate_response_length(message_length)

        # room_id에 따른 system_message 선택
        system_message = {
            1: SYSTEM_MESSAGE_1,
            2: SYSTEM_MESSAGE_2,
            3: SYSTEM_MESSAGE_3,
            4: SYSTEM_MESSAGE_4,
        }.get(room_id, SYSTEM_MESSAGE_1)

        # format을 사용하여 값 치환
        formatted_system_message = system_message.format(
            max_response_length=max_response_length
        )
        formatted_user_template = USER_MESSAGE_TEMPLATE.format(
            max_response_length=max_response_length,
            history="{history}",
            emotion="{emotion}",
            message="{message}",
        )

        # Redis 메모리 설정
        message_history = RedisChatMessageHistory(
            session_id=f"chat:{user_id}:{room_id}",
            url=settings.REDIS_URL,
            ttl=604800,
        )

        memory = ConversationBufferMemory(
            chat_memory=message_history,
            memory_key="history",
            return_messages=True,
            output_key="answer",
            input_key="message",
            k=50,
        )

        # 첫 번째 프롬프트
        first_prompt = ChatPromptTemplate.from_messages(
            [("system", formatted_system_message), ("human", formatted_user_template)]
        )

        # 두 번째 프롬프트
        validation_user_template = NATURAL_RESPONSE_TEMPLATE.format(
            max_response_length=max_response_length,
            history="{history}",
            emotion="{emotion}",
            message="{message}",
            answer="{answer}",
        )

        validation_prompt = ChatPromptTemplate.from_messages(
            [("system", formatted_system_message), ("human", validation_user_template)]
        )

        # 체인 구성
        chain = (
            RunnablePassthrough()
            | {
                "original_input": RunnablePassthrough(),
                "first_response": first_prompt
                | (ChatService.llm_t if room_id % 2 else ChatService.llm_f),
            }
            | (
                lambda x: {
                    **x["original_input"],
                    "answer": x["first_response"].content,
                }
            )
            | validation_prompt
            | ChatService.llm
            | (lambda x: x.content)
        )

        # 대화 기록 불러오기
        history = memory.load_memory_variables({})["history"]
        if history:
            formatted_history = "\n".join([msg.content for msg in history])
        else:
            formatted_history = "no"
        logging.info(f">>>>>>> Current History: {formatted_history}")

        # 응답 생성
        chain_input = {
            "max_response_length": max_response_length,
            "history": formatted_history,
            "emotion": emotion,
            "message": message,
        }
        logging.info(f">>>>>>> Chain Input: {chain_input}")

        answer = await chain.ainvoke(chain_input)
        memory.save_context(
            {"message": f"user: {message}"},
            {"answer": f"ai({ai_name[room_id]}): {answer}"},
        )

        logging.info(f">>>>>>> {answer}")

        # Kafka에 채팅 데이터 전송
        array_anwer = [s.strip() for s in answer.split("<br>")]
        for aa in array_anwer:
            KafkaService.send_to_kafka(user_id, room_id, aa)

        return {"response": array_anwer}

    ######################################

    @staticmethod
    def delete_chat_history(user_id: str, room_id: int):
        message_history = RedisChatMessageHistory(
            session_id=f"chat:{user_id}:{room_id}",
            url=settings.REDIS_URL,
            ttl=604800,
        )
        message_history.clear()
        logging.info(
            f">>>>>>> Deleted chat history for user {user_id} in room {room_id}"
        )

    @staticmethod
    def get_chat_history(user_id: str, room_id: int):
        message_history = RedisChatMessageHistory(
            session_id=f"chat:{user_id}:{room_id}",
            url=settings.REDIS_URL,
            ttl=604800,
        )

        memory = ConversationBufferMemory(
            chat_memory=message_history,
            memory_key="history",
            return_messages=True,
            output_key="answer",
            input_key="message",
            k=50,
        )

        history = memory.load_memory_variables({})["history"]
        if history:
            formatted_history = [msg.content for msg in history]
            logging.info(
                f">>>>>>> Loaded chat history for user {user_id} in room {room_id}"
            )
            return formatted_history
        return []


############################################################################
class MessageProcessor:

    @staticmethod
    def calculate_response_length(message_length: int) -> int:
        """메시지 길이에 따른 토큰 제한"""
        if message_length <= 20:
            return 20
        elif message_length <= 50:
            return 40
        else:
            return max(90, min(200, int(message_length * 0.5)))

    @staticmethod
    async def get_emotion(text: str) -> str:
        if len(text) < 5:
            return "기타"

        async with httpx.AsyncClient() as client:
            try:
                response = await client.post(
                    settings.EMOTION_URL,
                    json={"text": text},
                    headers={
                        "accept": "application/json",
                        "Content-Type": "application/json",
                    },
                    timeout=5.0,
                )
                response.raise_for_status()
                return response.text
            except Exception as e:
                logging.error(f"Error calling emotion API: {e}")
                return "기타"
