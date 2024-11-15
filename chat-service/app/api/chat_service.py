import httpx
from langchain_core.messages import SystemMessage, HumanMessage
from langchain.prompts import ChatPromptTemplate

from app.api.kafka_service import KafkaService
from app.core.logger import setup_logger
from app.core.config import settings
from langchain_openai import ChatOpenAI
from langchain.prompts import PromptTemplate
from langchain.schema.runnable import RunnablePassthrough

from app.models.templates import (
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
        temperature=0.4,
    )

    @staticmethod
    def create_chat_chain(system_message: str):
        """전체 프로세스를 하나의 체인으로 결합"""
        llm = ChatService.llm

        # 첫 번째 프롬프트
        first_prompt = ChatPromptTemplate.from_messages(
            [("system", system_message), ("human", USER_MESSAGE_TEMPLATE)]
        )

        # 두 번째 프롬프트
        validation_prompt = PromptTemplate(
            input_variables=[
                "max_response_length",
                "history",
                "relevant_info",
                "emotion",
                "message",
                "answer",
            ],
            template=NATURAL_RESPONSE_TEMPLATE,
        )

        # 체인 구성
        chain = (
            RunnablePassthrough()
            | {
                "original_input": RunnablePassthrough(),
                "first_response": first_prompt | llm,
            }
            | (
                lambda x: {
                    **x["original_input"],
                    "answer": x["first_response"].content,
                }
            )
            | validation_prompt
            | llm
            | (lambda x: x.content)
        )

        return chain

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

    @staticmethod
    async def process_chat(type: int, user_id: str, message: str):
        # 감정 분석
        emotion = await ChatService.get_emotion(message)
        logging.info(f">>>>>>> ({emotion}) {message}")

        # LLM 설정
        message_length = len(message)
        max_response_length = ChatService.calculate_response_length(message_length)

        # 체인 생성
        system_message = {
            1: SYSTEM_MESSAGE_1,
            2: SYSTEM_MESSAGE_2,
            3: SYSTEM_MESSAGE_3,
            4: SYSTEM_MESSAGE_4,
        }.get(type, SYSTEM_MESSAGE_1)

        chain = ChatService.create_chat_chain(system_message)

        # 첫 번째 응답 생성
        chain_input = {
            "max_response_length": max_response_length,
            "history": "no",
            "relevant_info": "no",
            "emotion": emotion,
            "message": message,
        }

        answer = await chain.ainvoke(chain_input)

        logging.info(f">>>>>>> {answer}")

        # Kafka에 채팅 데이터 전송
        array_anwer = [s.strip() for s in answer.split("<br>")]
        for aa in array_anwer:
            KafkaService.send_to_kafka(user_id, type, aa)

        return array_anwer
