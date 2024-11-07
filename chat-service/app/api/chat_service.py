import httpx
from langchain_core.messages import SystemMessage, HumanMessage
from app.core.logger import setup_logger
from app.core.config import settings
from langchain_openai import ChatOpenAI
from langchain.prompts import PromptTemplate
from langchain.chains import SequentialChain, LLMChain
from langchain.schema.runnable import RunnablePassthrough

from app.models.memory_manager import UserMemoryManager
from app.models.templates import (
    SYSTEM_MESSAGE_1,
    SYSTEM_MESSAGE_2,
    SYSTEM_MESSAGE_3,
    SYSTEM_MESSAGE_4,
    USER_MESSAGE_TEMPLATE,
    NATURAL_RESPONSE_TEMPLATE,
)


logging = setup_logger("app")


class ChatService:
    OPENAI_API_KEY = settings.GPT_KEY
    GPT_MODEL = "gpt-4o-mini"

    @staticmethod
    def calculate_response_length(message_length: int) -> int:
        """메시지 길이에 따른 응답 길이 계산"""
        if message_length <= 20:
            return message_length * 2
        elif message_length <= 50:
            return message_length * 1.7
        else:  # 긴 메시지
            return message_length * 1.5

    @staticmethod
    async def get_emotion(text: str) -> str:
        async with httpx.AsyncClient() as client:
            try:
                response = await client.post(
                    settings.EMOTION_URL,
                    json={"text": text},
                    headers={
                        "accept": "application/json",
                        "Content-Type": "application/json",
                    },
                    timeout=8.0,
                )
                response.raise_for_status()
                return response.text
            except Exception as e:
                logging.error(f"Error calling emotion API: {e}")
                return "기타"

    @staticmethod
    def get_memory_manager():
        # static variable 활용
        if not hasattr(ChatService, "_memory_manager"):
            ChatService._memory_manager = UserMemoryManager(
                llm=ChatOpenAI(api_key=ChatService.OPENAI_API_KEY, temperature=0.7)
            )
        return ChatService._memory_manager

    @staticmethod
    def get_llm(max_response_length: int):
        return ChatOpenAI(
            api_key=ChatService.OPENAI_API_KEY,
            model_name=ChatService.GPT_MODEL,
            temperature=0.7,
        )

    @staticmethod
    def create_response_chain(llm, template: str) -> LLMChain:
        response_prompt = PromptTemplate(
            input_variables=[
                "max_length",
                "history",
                "relevant_info",
                "emotion",
                "message",
            ],
            template=template,
        )
        return response_prompt | llm | {"answer": RunnablePassthrough()}

    @staticmethod
    def create_validation_chain(llm) -> LLMChain:
        """Creates the validation chain"""
        validation_prompt = PromptTemplate(
            input_variables=[
                "max_length",
                "history",
                "relevant_info",
                "emotion",
                "message",
                "answer",
            ],
            template=NATURAL_RESPONSE_TEMPLATE,
        )
        return validation_prompt | llm | {"validation": RunnablePassthrough()}

    @staticmethod
    async def process_chat(type: int, user_id: str, message: str):
        # 감정 분석
        emotion = await ChatService.get_emotion(message)
        logging.info(f">>>>>>> ({emotion}) {message}")

        # LLM 및 메모리 설정
        message_length = len(message)
        max_response_length = ChatService.calculate_response_length(message_length)
        llm = ChatService.get_llm(max_response_length)
        # memory_manager = ChatService.get_memory_manager()

        # 메모리 처리
        # short_term, entity_memory, vector_store = memory_manager.get_or_create_memories(
        #     str(user_id)
        # )
        # recent_history = short_term.chat_memory.messages

        # entity_memory.save_context({"input": message}, {"output": ""})
        # memory_variables = entity_memory.load_memory_variables({"input": message})
        # entity_memory.save_context(
        #     {"input": message}, {"output": "User message processed"}
        # )
        # entities = memory_variables.get("entities", {})

        # if entities:
        #     vector_store.add_texts(
        #         [f"User preference: {json.dumps(entities, ensure_ascii=False)}"]
        #     )
        # vector_store.add_texts([message])

        # 관련 정보 검색
        # relevant_docs = vector_store.similarity_search(message, k=2)
        # relevant_history = [doc.page_content for doc in relevant_docs]

        # logging.info(f"recent_history : \n{recent_history}")
        # logging.info(f"relevant_docs : \n{relevant_docs}")
        # logging.info(f"relevant_history : \n{relevant_history}")

        # 체인 생성
        system_message = {
            1: SYSTEM_MESSAGE_1,
            2: SYSTEM_MESSAGE_2,
            3: SYSTEM_MESSAGE_3,
            4: SYSTEM_MESSAGE_4,
        }.get(type, SYSTEM_MESSAGE_1)

        response_chain = ChatService.create_response_chain(llm, system_message)
        validation_chain = ChatService.create_validation_chain(llm)

        messages = [
            SystemMessage(content=system_message),
            HumanMessage(
                content=USER_MESSAGE_TEMPLATE.format(
                    emotion=emotion,
                    message=message,
                    history="no",
                    relevant_info="no",
                    max_response_length=max_response_length // 3,
                )
            ),
        ]
        answer = llm.invoke(messages).content
        # short_term.save_context({"input": message}, {"output": answer})

        # 응답 생성
        # result = await response_chain.ainvoke(messages)

        input_dict = {
            "max_length": max_response_length // 3,
            "history": "no",
            "relevant_info": "no",
            "emotion": emotion,
            "message": message,
        }

        validation_input = {**input_dict, "answer": answer}
        validation_result = await validation_chain.ainvoke(validation_input)

        # 답변 최종 선택(1에서 괜찮으면 그대로, 문제가 있으면 수정본으로)
        logging.info(f">>>>>>> (수정 전) {answer}")
        final_answer = (
            answer
            if validation_result["validation"].content == "Yes"
            else validation_result["validation"].content
        )
        logging.info(f">>>>>>> (수정 후) {final_answer}")
        # short_term.save_context({"input": message}, {"output": final_answer})

        return {"response": [s.strip() for s in final_answer.split("<br>")]}
