package com.survivalcoding.a510.services

import android.util.Log
import com.survivalcoding.a510.data.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.synchronized

class TokenInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {
    private var isRefreshing = false

    override fun intercept(chain: Interceptor.Chain): Response {
        // 원본 요청에 액세스 토큰 추가
        val originalRequest = chain.request()

        val request = addAuthHeader(originalRequest, tokenManager.getAccessToken())
        Log.d("리퀘스트 찍기", "내가 보낸 리퀘스트: $request")


        // 요청 실행
        var response = chain.proceed(request)

        // 401 Unauthorized 응답을 받은 경우
        if (response.code == 401) {
            synchronized(this) {
                // 다른 스레드에서 이미 토큰을 갱신중인지 확인
                if (!isRefreshing) {
                    isRefreshing = true
                    try {
                        // 리프레시 토큰으로 새 액세스 토큰 발급 시도
                        val newAccessToken = refreshToken()
                        if (newAccessToken != null) {
                            // 새 액세스 토큰으로 원본 요청 재시도
                            val newRequest = addAuthHeader(originalRequest, newAccessToken)
                            response.close()
                            response = chain.proceed(newRequest)
                        }
                    } finally {
                        isRefreshing = false
                    }
                } else {
                    // 다른 스레드가 토큰을 갱신하는 동안 대기
                    while (isRefreshing) {
                        Thread.sleep(100)
                    }
                    // 갱신된 토큰으로 요청 재시도
                    val newRequest = addAuthHeader(originalRequest, tokenManager.getAccessToken())
                    response.close()
                    response = chain.proceed(newRequest)
                }
            }
        }

        return response
    }

    private fun addAuthHeader(request: Request, token: String?): Request {
        return if (token != null) {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }
    }

    private fun refreshToken(): String? {
        return try {
            val retrofit = Retrofit.Builder()
                .baseUrl("http://k11a510.p.ssafy.io:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val authService = retrofit.create(AuthService::class.java)

            // 동기적으로 토큰 갱신 요청 실행
            val response = runBlocking {
                authService.refreshToken()
            }

            if (response.isSuccessful) {
                // 새로운 액세스 토큰은 응답 헤더에서 추출
                val newAccessToken = response.headers()["Authorization"]?.removePrefix("Bearer ")
                if (newAccessToken != null) {
                    // TokenManager에 새 토큰 저장
                    tokenManager.saveTokens(newAccessToken, tokenManager.getRefreshToken() ?: "")
                    newAccessToken
                } else {
                    null
                }
            } else {
                // 토큰 갱신 실패 시 로그아웃 처리
                tokenManager.clearTokens()
                null
            }
        } catch (e: Exception) {
            Log.e("TokenInterceptor", "Token refresh failed", e)
            // 예외 발생 시 로그아웃 처리
            tokenManager.clearTokens()
            null
        }
    }
}