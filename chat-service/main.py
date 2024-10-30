from fastapi import FastAPI
import uvicorn
import chat_controller

app = FastAPI()
app.include_router(chat_controller.router)

@app.get("/health-check")
def read_root():
    return {"Hello": "World"}


if __name__ == '__main__':
    config = uvicorn.Config("main:app", port=8001, log_level="info", reload=True)
    server = uvicorn.Server(config)
    server.run()