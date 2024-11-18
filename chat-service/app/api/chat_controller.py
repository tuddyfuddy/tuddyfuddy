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


# 임시용
@router.post("/test/{room_id}")
@log_api_time
async def chat(
    room_id: int,
    request: TextRequest,
    user_id: str = Query(...),
):
    try:
        result = await ChatService.process_chat(room_id, user_id, request.text)
        if isinstance(result, JSONResponse):
            return ["응?"]
        return result
    except Exception as e:
        logging.error(f"Error in chat endpoint: {e}")
        return ["응?"]


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
            return ["응?"]
        return result
    except Exception as e:
        logging.error(f"Error in chat endpoint: {e}")
        return ["응?"]


@router.post("/group/{type}")
@log_api_time
async def chat(
    type: int,
    request: TextRequest,
    user_info: UserHeaderInfo = Depends(get_user_header_info),
):
    try:
        return ["응?"]
    except Exception as e:
        return ["응?"]
