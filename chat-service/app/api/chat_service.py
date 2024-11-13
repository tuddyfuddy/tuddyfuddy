import json

import httpx
from confluent_kafka import Producer
from langchain_core.messages import SystemMessage, HumanMessage
from app.core.logger import setup_logger
from app.core.config import settings
from langchain_openai import ChatOpenAI
from langchain.prompts import PromptTemplate
from langchain.chains import LLMChain
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

    # Kafka Producer 초기화
    producer_config = {
        "bootstrap.servers": "kafka:9092",
        "socket.timeout.ms": 1000,
        "message.timeout.ms": 1000,
        "retries": 2,
        "retry.backoff.ms": 100,
        "request.required.acks": 0,
        "queue.buffering.max.ms": 0,
    }
    producer = Producer(producer_config)

    # LLM  초기화
    llm = ChatOpenAI(
        api_key=settings.GPT_KEY,
        model_name="gpt-4o-mini",
        temperature=0.4,
    )

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
    def create_validation_chain(llm) -> LLMChain:
        """Creates the validation chain"""
        validation_prompt = PromptTemplate(
            input_variables=[
                "max_length",
                "history",
                "relevant_info",
                "emotion",
                "message",
                "answer",
            ],
            template=NATURAL_RESPONSE_TEMPLATE,
        )
        return validation_prompt | llm | {"validation": RunnablePassthrough()}

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

        validation_chain = ChatService.create_validation_chain(ChatService.llm)

        # 첫 번째 응답 생성
        messages = [
            SystemMessage(content=system_message),
            HumanMessage(
                content=USER_MESSAGE_TEMPLATE.format(
                    emotion=emotion,
                    message=message,
                    history="no",
                    relevant_info="no",
                    max_response_length=max_response_length,
                )
            ),
        ]
        answer = ChatService.llm.invoke(messages).content
        logging.info(f">>>>>>> (수정 전) {answer}")

        # 두 번째 응답 생성
        validation_input = {
            "max_length": max_response_length,
            "history": "no",
            "relevant_info": "no",
            "emotion": emotion,
            "message": message,
            "answer": answer,
        }
        validation_result = await validation_chain.ainvoke(validation_input)

        final_answer = validation_result["validation"].content
        logging.info(f">>>>>>> (수정 후) {final_answer}")

        # Kafka에 채팅 데이터 전송
        array_anwer = [s.strip() for s in final_answer.split("<br>")]
        for aa in array_anwer:
            ChatService.send_to_kafka(user_id, type, aa)

        return {"response": [s.strip() for s in final_answer.split("<br>")]}

    @staticmethod
    def send_to_kafka(user_id: str, room_id: int, message: str):
        try:
            chat_data = {
                "userId": str(user_id),
                "roomId": room_id,
                "aiName": ai_name[room_id],
                "message": message,
            }

            ChatService.producer.produce(
                "chat-notification-topic",
                value=json.dumps(chat_data).encode("utf-8"),
                callback=lambda err, msg: (
                    logging.error(f"Failed to send message: {err}") if err else None
                ),
            )
        except Exception as e:
            logging.error(f"Failed to send chat data to Kafka (non-critical): {e}")
