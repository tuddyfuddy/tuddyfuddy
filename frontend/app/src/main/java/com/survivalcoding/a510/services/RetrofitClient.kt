package com.survivalcoding.a510.services

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//object RetrofitClient {
//    private const val BASE_URL = "https://your-api-server.com/"
//
//    private val retrofit = Retrofit.Builder()
//        .baseUrl(BASE_URL)
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()
//
//    val authService: AuthService = retrofit.create(AuthService::class.java)
//}

object RetrofitClient {
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
}