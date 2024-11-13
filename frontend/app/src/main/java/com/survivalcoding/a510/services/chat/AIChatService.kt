package com.survivalcoding.a510.services.chat

import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.Query

/**
 * 채팅 요청을 위한 데이터 클래스
 */
data class ChatRequest(
    val text: String
)

/**
 * 채팅 API 응답을 위한 데이터 클래스
 * 응답이 문자열 리스트로 옴
 */
data class ChatResponse(
    val response: List<String>  // String List<String>으로 변경
)

/**
 * ChatResponse의 응답 메시지를 리스트로 변환하는 확장 함수
 */
fun ChatResponse.getMessageList(): List<String> {
    return response  // 이미 리스트로 오기 때문에 바로 반환
}

interface AIChatService {
    @POST("chats/direct/{type}")
    suspend fun sendChatMessage(
        @Path("type") type: Int,
        @Body request: ChatRequest
    ): Response<ChatResponse>
}