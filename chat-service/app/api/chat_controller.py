from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse
from pydantic import BaseModel

from app.api.header_dependencies import get_user_header_info, UserHeaderInfo
from app.core.logger import setup_logger
from app.api.chat_service import ChatService


router = APIRouter(prefix="/chat", tags=["chat"])
logging = setup_logger("app")


class TextRequest(BaseModel):
    text: str


@router.post("/chats/direct/{type}")
async def chat(
    type: int,
    request: TextRequest,
    user_info: UserHeaderInfo = Depends(get_user_header_info),
):
    try:
        result = await ChatService.process_chat(type, user_info.user_id, request.text)
        if isinstance(result, JSONResponse):
            return ["응?"]
        return result
    except Exception as e:
        logging.error(f"Error in chat endpoint: {e}")
        return ["응?"]


@router.post("/chats/group/{type}")
async def chat(
    type: int,
    request: TextRequest,
    user_info: UserHeaderInfo = Depends(get_user_header_info),
):
    try:
        return ["응?"]
    except Exception as e:
        return ["응?"]


@router.get("/history/{user_id}")
async def get_history(user_id: str):
    try:
        result = await ChatService.get_history(user_id)
        if isinstance(result, JSONResponse):
            return result
        return result
    except Exception as e:
        logging.error(f"Error in history endpoint: {e}")
        return JSONResponse(
            status_code=500,
            content={"error": "Failed to fetch chat history", "detail": str(e)},
        )
