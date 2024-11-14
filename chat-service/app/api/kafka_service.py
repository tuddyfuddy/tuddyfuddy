import threading
import json

from langchain_core.messages import HumanMessage

from app.core.config import settings
from langchain_openai import ChatOpenAI

from app.models.templates import (
    WEATHER_RESPONSE_TEMPLATE,
    CALENDAR_RESPONSE_TEMPLATE,
)


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
    location: str
    todayWeather: WeatherData
    yesterdayWeather: WeatherData


class WeatherMessage(BaseModel):
    userId: str
    data: Weathers


class CalendarMessage(BaseModel):
    userId: str
    todo: str


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
        temperature=0.8,
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
        except Exception as e:
            logging.error(f"Failed to send chat data to Kafka (non-critical): {e}")

    ########################################################################################

    @staticmethod
    async def process_calendar_chat(type: int, user_id: str, calendar_data: dict):
        calendar_response = KafkaService.llm.invoke(
            [
                HumanMessage(
                    content=CALENDAR_RESPONSE_TEMPLATE.format(
                        calendar_data=json.dumps(calendar_data, ensure_ascii=False)
                    )
                )
            ]
        ).content

        logging.info(f">>>>>>> (일정 응답) {calendar_response}")

        array_answer = [s.strip() for s in calendar_response.split("<br>")]
        for answer in array_answer:
            KafkaService.send_to_kafka(user_id, type, answer)

    @staticmethod
    async def process_weather_chat(type: int, user_id: str, calendar_data: dict):
        weather_response = KafkaService.llm.invoke(
            [
                HumanMessage(
                    content=WEATHER_RESPONSE_TEMPLATE.format(
                        calendar_data=json.dumps(calendar_data, ensure_ascii=False)
                    )
                )
            ]
        ).content

        logging.info(f">>>>>>> (날씨 응답) {weather_response}")

        array_answer = [s.strip() for s in weather_response.split("<br>")]
        for answer in array_answer:
            KafkaService.send_to_kafka(user_id, type, answer)

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
                    KafkaService.process_calendar_chat(
                        2, value["userId"], value["todo"]
                    )
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
                    KafkaService.process_weather_chat(2, value["userId"], value["data"])

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
