package com.survivalcoding.a510.services

import com.survivalcoding.a510.services.chat.AIChatService
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://k11a510.p.ssafy.io:8080/chat-service/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // 테스트용 가짜 응답
    fun mockAuthService() = object : AuthService {
        override suspend fun authenticateKakao(request: KakaoAuthRequest): Response<KakaoAuthResponse> {
            // 항상 성공 응답 반환
            return Response.success(
                KakaoAuthResponse(
                    token = "fake_jwt_token",
                    userId = "12345",
                    nickname = "테스트유저"
                )
            )
        }
    }

    // 실제 서비스 대신 가짜 서비스 사용
    val authService: AuthService = mockAuthService()

    // AI 채팅 서비스 추가
    val aiChatService: AIChatService = retrofit.create(AIChatService::class.java)
}