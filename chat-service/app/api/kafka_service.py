import json
import threading

from confluent_kafka import Producer, Consumer
from app.core.logger import setup_logger
from app.models.templates import ai_name
from pydantic import BaseModel

logging = setup_logger("app")

KAFKA_SERVERS = "kafka:9092"
KAFKA_GROUP = "context-group"

calendar_consumer = Consumer(
    {
        "bootstrap.servers": KAFKA_SERVERS,
        "group.id": KAFKA_GROUP,
        "auto.offset.reset": "earliest",
        "enable.auto.commit": True,
    }
)
calendar_consumer.subscribe(["chat-calendar-topic"])

weather_consumer = Consumer(
    {
        "bootstrap.servers": KAFKA_SERVERS,
        "group.id": KAFKA_GROUP,
        "auto.offset.reset": "earliest",
        "enable.auto.commit": True,
    }
)
weather_consumer.subscribe(["chat-weather-topic"])


# 스키마 정의
class WeatherData(BaseModel):
    x: int
    y: int
    timestamp: str
    minTemperature: int
    maxTemperature: int
    weather: str
    note: str
    createdAt: str


class Weathers(BaseModel):
    todayWeather: WeatherData
    yesterdayWeather: WeatherData


class WeatherMessage(BaseModel):
    userId: str
    location: str
    weathers: Weathers


class CalendarMessage(BaseModel):
    userId: str
    todo: str


class KafkaService:

    # Kafka Producer 초기화
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

    ########################################################################################

    @staticmethod
    def consume_calendar_messages():
        """캘린더 메시지 소비"""
        while True:
            try:
                msg = calendar_consumer.poll(0)
                if msg and not msg.error():
                    value = json.loads(msg.value().decode("utf-8"))
                    logging.info(f"캘린더 메시지 도착: {value}")
                    # TODO 캘린더 메시지 발송 로직 구현
            except Exception as e:
                logging.error(f"Error processing calendar message: {e}")

    @staticmethod
    def consume_weather_messages():
        """날씨 메시지 소비"""
        while True:
            try:
                msg = weather_consumer.poll(0)
                if msg and not msg.error():
                    value = json.loads(msg.value().decode("utf-8"))
                    logging.info(f"날씨 메시지 도착: {value}")
                    # TODO 날씨 메시지 발송 로직 구현
            except Exception as e:
                logging.error(f"Error processing weather message: {e}")

    #################################################################################


def start_consumers():
    """Consumer 스레드 시작"""
    calendar_thread = threading.Thread(
        target=KafkaService.consume_calendar_messages, daemon=True
    )
    weather_thread = threading.Thread(
        target=KafkaService.consume_weather_messages, daemon=True
    )

    calendar_thread.start()
    weather_thread.start()


def close_consumers():
    """Consumer 종료"""
    calendar_consumer.close()
    weather_consumer.close()
