package com.survivalcoding.a510.services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("auth/kakao")
    suspend fun authenticateKakao(
        @Body request: KakaoAuthRequest
    ): Response<KakaoAuthResponse>
}

data class KakaoAuthRequest(
    val accessToken: String
)

data class KakaoAuthResponse(
    val token: String,
    val userId: String,
    val nickname: String
)

