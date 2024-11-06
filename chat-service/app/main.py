from fastapi import FastAPI
from py_eureka_client import eureka_client
from core.config import settings
from api import chat_controller

import uvicorn
import os

app = FastAPI(
    root_path="/chat-service",
    openapi_url="/v3/api-docs",  # OpenAPI 문서 경로 설정
    docs_url="/docs",  # Swagger UI 경로 설정
)
app.include_router(chat_controller.router)


async def register_to_eureka():
    # Docker 환경에서는 hostname이 container name이 되어야 함
    hostname = os.getenv("HOSTNAME", "chat-service")
    try:
        await eureka_client.init_async(
            eureka_server=settings.EUREKA_SERVER_URL,
            app_name=settings.SERVICE_NAME,
            instance_port=settings.SERVICE_PORT,
            instance_host=hostname,
            status_page_url=f"http://{hostname}:{settings.SERVICE_PORT}/docs",
            health_check_url=f"http://{hostname}:{settings.SERVICE_PORT}/health-check",
        )
        print(f"Successfully registered with Eureka at {settings.EUREKA_SERVER_URL}")
    except Exception as e:
        print(f"Failed to register with Eureka: {e}")


@app.get("/health-check")
def read_root():
    return {"Hello": "World"}


@app.on_event("startup")
async def startup_event():
    if not app.openapi_schema:
        openapi_schema = app.openapi()
        openapi_schema["components"]["securitySchemes"] = {
            "API Header": {
                "type": "apiKey",
                "name": "x-userid",
                "in": "header",
            }
        }
        openapi_schema["servers"] = [{"url": "/chat-service"}]  # chat-service로 변경
        openapi_schema["security"] = [{"API Header": []}]
        app.openapi_schema = openapi_schema

    await register_to_eureka()


@app.on_event("shutdown")
async def shutdown_event():
    await eureka_client.stop()


if __name__ == "__main__":
    config = uvicorn.Config("main:app", port=8000, log_level="info", reload=True)
    server = uvicorn.Server(config)
    server.run()
