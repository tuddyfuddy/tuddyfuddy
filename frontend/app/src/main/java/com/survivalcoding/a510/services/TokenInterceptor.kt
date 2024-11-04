package com.survivalcoding.a510.services

import com.survivalcoding.a510.data.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

// 모든 요청에 자동으로 Authorization 헤더를 추가하는 인터셉터
class TokenInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${tokenManager.getAccessToken()}")
            .build()
        return chain.proceed(request)
    }
}