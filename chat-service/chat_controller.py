from typing import Dict
from collections import deque
from fastapi import APIRouter
import requests
from fastapi.responses import JSONResponse

router = APIRouter(prefix="/chat", tags=["chat"])

# TODO key env로 변경
OPENAI_API_KEY = "."
API_URL = "https://api.openai.com/v1/chat/completions"
GPT_MODEL = "gpt-4o-mini"
MAX_HISTORY = 10  # 최대 저장할 대화 기록 수


def calculate_response_length(message_length: int) -> int:
    """메시지 길이에 따른 적절한 응답 길이 계산"""
    if message_length <= 20:  # 짧은 메시지
        return message_length * 2
    elif message_length <= 50:  # 중간 길이 메시지
        return message_length * 1.7
    else:  # 긴 메시지
        return message_length * 1.5


SYSTEM_PROMPT_TEMPLATE = """
Start a casual conversation, and respond as comfortably as a close friend to express your feelings honestly. Relate to the feelings, but avoid overly friendly or exaggerated expressions, and continue the conversation naturally. Make the conversation light and comfortable as a real friend.
Topic: conversation
Style: Casual
Tone: Friendly, relaxed, and not overly formal
Audience: 20-year old
Response Length: Up to {max_length} characters
Format: Text

User emotions are given one of joy/calm/sad/angry/anxiety/tired.
- Calm: Continue the conversation naturally without any particular reaction to your emotions.
- Joy/Angry: Please share your feelings and respond.
- Sadness/Anxiety/Tired: Please say words of consolation.

When printing, make sure to change lines into sentences or phrases and send them in the form of messengers. Speak informally in Korean.

Please remember the conversation below and continue with the flow:

Previous Conversations:
{history}

current situation:
emotion: {emotion}
message: {message}
"""

# TODO DB로 관리
conversation_history = deque(maxlen=MAX_HISTORY)


def format_history(history: deque) -> str:
    if not history:
        return "이전 대화 없음"
    recent_history = list(history)[-5:]
    return "\n".join([
        f"user: {conv['query']}\nassistant: {conv['response']}"
        for conv in recent_history
    ])


@router.post("/chat")
async def chat(emotion: str, message: str):
    try:
        history = format_history(conversation_history)

        message_length = len(message)
        max_response_length = int(calculate_response_length(message_length))

        system_prompt = SYSTEM_PROMPT_TEMPLATE.format(
            max_length=max_response_length,
            history=history,
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

        conversation_history.append({
            "query": message,
            "response": answer
        })

        return {
            "response": answer
        }

    except Exception as e:
        return JSONResponse(status_code=500, content=str(e))


@router.get("/history")
async def get_history():
    return {"history": format_history(conversation_history)}


@router.delete("/clear")
async def clear_history():
    conversation_history.clear()
    return {"message": "대화 기록이 초기화되었습니다."}