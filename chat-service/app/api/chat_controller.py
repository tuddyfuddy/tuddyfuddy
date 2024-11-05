from app.core.config import settings
from collections import deque
from fastapi import APIRouter
import requests
from fastapi.responses import JSONResponse
from pydantic import BaseModel
import httpx
from app.core.logger import setup_logger


router = APIRouter(prefix="/chat", tags=["chat"])
OPENAI_API_KEY = settings.GPT_KEY
API_URL = "https://api.openai.com/v1/chat/completions"
GPT_MODEL = "gpt-4o-mini"
MAX_HISTORY = 10  # 최대 저장할 대화 기록 수

class TextRequest(BaseModel):
    text: str

logging = setup_logger("app")

def calculate_response_length(message_length: int) -> int:
    """메시지 길이에 따른 응답 길이 계산"""
    if message_length <= 20:
        return message_length * 2
    elif message_length <= 50:
        return message_length * 1.7
    else:  # 긴 메시지
        return message_length * 1.5


SYSTEM_PROMPT_TEMPLATE_1 = """

"""

SYSTEM_PROMPT_TEMPLATE_2 = """
Start a casual conversation, and respond as comfortably as a close friend to express your feelings honestly. Relate to the feelings, but avoid overly friendly or exaggerated expressions, and continue the conversation naturally. Make the conversation light and comfortable as a real friend.
Topic: conversation
Style: Casual
Tone: Friendly, relaxed, and not overly formal
Audience: 20-year old
Response Length: Up to {max_length} characters
Format: Text

User emotions are given one of 분노/놀람/행복/공포/슬픔/기타.
- 기타: Continue the conversation naturally without any particular reaction to your emotions.
- 분노/행복: Please share your feelings and respond.
- 놀람/공포/슬픔: Please say words of consolation.

When printing, make sure to add '<br>' between sentences or phrases and send them in the form of messengers. 
Each complete sentence must end with a final ending word(종결어미), Speak informally in Korean
Please remember the conversation below and continue with the flow:
Previous Conversations:
{history}

current situation:
emotion: {emotion}
message: {message}
"""

# TODO DB로 관리
conversation_history_1 = deque(maxlen=MAX_HISTORY)
conversation_history_2 = deque(maxlen=MAX_HISTORY)

async def get_emotion(text: str) -> str:
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                settings.EMOTION_URL,
                json={"text": text},
                headers={'accept': 'application/json', 'Content-Type': 'application/json'},
                timeout = 8.0
            )
            response.raise_for_status()
            return response.text
        except Exception as e:
            logging.error(f"Error calling emotion API: {e}")
            return "기타"



@router.post("/{type}")
async def chat(type:int, request: TextRequest):
    message = request.text
    emotion = await get_emotion(message)

    logging.info(f">>>>>>> {emotion}")
    logging.info(f">>>>>>> {message}")

    try:
        history = conversation_history_1 if type == 1 else conversation_history_2
        template = SYSTEM_PROMPT_TEMPLATE_1 if type == 1 else SYSTEM_PROMPT_TEMPLATE_2

        message_length = len(message)
        max_response_length = int(calculate_response_length(message_length))

        system_prompt = template.format(
            max_length=max_response_length,
            history=list(history),
            emotion=emotion,
            message=message
        )

        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {OPENAI_API_KEY}"
        }

        data = {
            "model": GPT_MODEL,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": message}
            ],
            "max_tokens": max_response_length + 50
        }

        response = requests.post(API_URL, headers=headers, json=data)
        response.raise_for_status()

        response_data = response.json()
        answer = response_data["choices"][0]["message"]["content"]

        history.append({"user": message})
        history.append({"assistant": answer})

        sentences = [sentence.strip() for sentence in answer.split("<br>")]

        return {
            "response": sentences
        }

    except Exception as e:
        return JSONResponse(status_code=500, content=str(e))


@router.get("/history/{type}")
async def get_history(type: int):
    history = conversation_history_1 if type == 1 else conversation_history_2
    return {"history": list(history)}


@router.delete("/clear/{type}")
async def clear_history(type:int):
    if type == 1 :
        conversation_history_1.clear()
    else :
        conversation_history_2.clear()
    return {"message": "대화 기록이 초기화되었습니다."}