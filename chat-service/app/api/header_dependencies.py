from fastapi import Request
from fastapi.responses import JSONResponse

from app.core.logger import setup_logger

logger = setup_logger("app")


class UserHeaderInfo:
    def __init__(self, user_id: str):
        self.user_id = user_id


def get_user_header_info(
    request: Request,
):
    user_id = request.headers.get("x-userid")
    if user_id is None:
        return JSONResponse(
            status_code=400,
            content={"status": "error", "message": "헤더에 토큰이 필요합니다"},
        )

    return UserHeaderInfo(user_id=user_id)
