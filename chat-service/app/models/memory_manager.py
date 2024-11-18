from langchain.memory import ConversationEntityMemory, ConversationBufferWindowMemory
from langchain_openai import OpenAIEmbeddings
from langchain_community.vectorstores import FAISS
from app.core.config import settings

OPENAI_API_KEY = settings.GPT_KEY


class UserMemoryManager:
    def __init__(self, llm):
        global OPENAI_API_KEY
        self.llm = llm
        self.short_term_memories = {}  # user_id -> BufferWindowMemory
        self.entity_memories = {}  # user_id -> EntityMemory
        self.vector_stores = {}  # user_id -> FAISS store
        self.embeddings = OpenAIEmbeddings(api_key=OPENAI_API_KEY)  # api_key로 변경

    def get_or_create_memories(self, user_id: str):
        # 해당 사용자의 메모리가 없으면 생성
        if user_id not in self.short_term_memories:
            self.short_term_memories[user_id] = ConversationBufferWindowMemory(
                k=5,
                return_messages=True,  # 이거 추가
                memory_key="chat_history",  # 이거 추가
            )
            self.entity_memories[user_id] = ConversationEntityMemory(
                llm=self.llm,
                input_key="input",
                output_key="output",
                return_messages=True,
                k=5,
            )
            self.vector_stores[user_id] = FAISS.from_texts(["초기화"], self.embeddings)

        return (
            self.short_term_memories[user_id],
            self.entity_memories[user_id],
            self.vector_stores[user_id],
        )
