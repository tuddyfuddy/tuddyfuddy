from fastapi import APIRouter, Depends, Query
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from app.core.uilts import log_api_time
from app.api.header_dependencies import get_user_header_info, UserHeaderInfo
from app.core.logger import setup_logger
from app.api.chat_service import ChatService


router = APIRouter(prefix="/chats", tags=["chat"])
logging = setup_logger("app")


class TextRequest(BaseModel):
    text: str


@router.post("/direct/{room_id}")
@log_api_time
async def chat(
    room_id: int,
    request: TextRequest,
    user_info: UserHeaderInfo = Depends(get_user_header_info),
):
    try:
        result = await ChatService.process_chat(
            room_id, user_info.user_id, request.text
        )
        if isinstance(result, JSONResponse):
            return {"response": ["응?"]}
        return result
    except Exception as e:
        logging.error(f"Error in chat endpoint: {e}")
        return {"response": ["응?"]}


############################################################################


@router.post("/test/{user_id}/{room_id}")
@log_api_time
async def chat(user_id: str, room_id: int, request: TextRequest):
    try:
        result = await ChatService.process_chat(room_id, user_id, request.text)
        if isinstance(result, JSONResponse):
            return {"response": ["응?"]}
        return result
    except Exception as e:
        logging.error(f"Error in chat endpoint: {e}")
        return {"response": ["응?"]}


@router.get("/history/{user_id}/{room_id}")
async def get_chat_history(user_id: str, room_id: int):
    history = ChatService.get_chat_history(user_id, room_id)
    return {"history": history}


@router.delete("/history/{user_id}/{room_id}")
async def delete_chat_history(user_id: str, room_id: int):
    ChatService.delete_chat_history(user_id, room_id)
    return {"message": "Chat history deleted successfully"}
