from fastapi import FastAPI
from py_eureka_client import eureka_client
from core.config import settings
import uvicorn
from api import chat_controller

app = FastAPI(
    root_path="/chat-service",
    openapi_url="/v3/api-docs",  # OpenAPI 문서 경로 설정
    docs_url="/docs",  # Swagger UI 경로 설정
)
app.include_router(chat_controller.router)

async def register_to_eureka():
    await eureka_client.init_async(
        eureka_server=f"{settings.EUREKA_SERVER_URL}",
        app_name="chat-service",
        instance_port=8000,
        instance_host="localhost"
    )

@app.get("/health-check")
def read_root():
    return {"Hello": "World"}

# @app.on_event("startup")
# async def startup_event():
    # await register_to_eureka()

# @app.on_event("shutdown")
# async def shutdown_event():
#     await eureka_client.stop()

if __name__ == '__main__':
    config = uvicorn.Config("main:app", port=8000, log_level="info", reload=True)
    server = uvicorn.Server(config)
    server.run()