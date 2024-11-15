import json


from app.core.config import settings
from langchain_openai import ChatOpenAI


from confluent_kafka import Producer
from app.core.logger import setup_logger
from app.models.templates import ai_name

logging = setup_logger("app")

KAFKA_SERVERS = "kafka:9092"


class KafkaService:

    producer_config = {
        "bootstrap.servers": KAFKA_SERVERS,
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
        temperature=1,
    )

    ########################################################################################
    @staticmethod
    def send_to_kafka(user_id: str, room_id: int, message: str):
        try:
            chat_data = {
                "userId": str(user_id),
                "roomId": room_id,
                "aiName": ai_name[room_id],
                "message": message,
            }

            KafkaService.producer.produce(
                "chat-notification-topic",
                value=json.dumps(chat_data).encode("utf-8"),
                callback=lambda err, msg: (
                    logging.error(f"Failed to send message: {err}") if err else None
                ),
            )

            # logging.info(f"Sending message to Kafka: {chat_data}")
        except Exception as e:
            logging.error(f"Failed to send chat data to Kafka (non-critical): {e}")


########################################################################################
