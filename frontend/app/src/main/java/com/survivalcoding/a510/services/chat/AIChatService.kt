package com.survivalcoding.a510.services.chat

import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 채팅 API 응답을 위한 데이터 클래스
 * @property response 서버로부터 받은 응답 메시지
 */
data class ChatResponse(
    val response: String
)

/**
 * ChatResponse의 응답 메시지를 개별 메시지 리스트로 변환하는 확장 함수
 * @return 줄바꿈으로 구분된 메시지 리스트
 */
fun ChatResponse.getMessageList(): List<String> {
    return this.response.split("\n")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}

/**
 * AI 채팅 서비스 인터페이스
 */
interface AIChatService {
    /**
     * 감정과 메시지를 보내고 서버로부터 응답을 받는 API 호출
     * @param type 채팅 타입
     * @param emotion 사용자의 감정 상태
     * @param message 사용자의 메시지
     * @return ChatResponse를 포함한 Response 객체
     */
    @POST("chat/chat/{type}")
    suspend fun sendChatMessage(
        @Path("type") type: Int,
        @Query("emotion") emotion: String,
        @Query("message") message: String
    ): Response<ChatResponse>
}