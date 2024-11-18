from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    EUREKA_SERVER_URL: str = "http://discovery-service:8761/eureka"
    SERVICE_NAME: str = "chat-service"
    SERVICE_PORT: int = 8000
    SERVICE_HOST: str = "chat-service" 
    
    # 데이터베이스 설정
    MONGODB_URL: str = "mongodb://chat-mongodb:27017/chatdb"
    REDIS_URL: str = "redis://chat-redis:6379"
    ELASTICSEARCH_URL: str = "http://chat-elasticsearch:9200"

    GPT_KEY: str = ""
    EMOTION_URL : str = ""

    class Config:
        env_file = ".env"
        # 추가 필드 허용
        extra = "allow"

settings = Settings()