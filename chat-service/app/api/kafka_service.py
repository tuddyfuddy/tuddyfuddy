import json

from confluent_kafka import Producer
from app.core.logger import setup_logger
from app.models.templates import ai_name

logging = setup_logger("app")


class KafkaService:

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
        except Exception as e:
            logging.error(f"Failed to send chat data to Kafka (non-critical): {e}")
