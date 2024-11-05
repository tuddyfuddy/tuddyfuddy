package com.survivalcoding.a510.services

import android.content.Context
import com.survivalcoding.a510.data.TokenManager
import com.survivalcoding.a510.services.chat.AIChatService
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Retrofit 인스턴스를 관리하는 싱글톤 객체
object RetrofitClient {
    private const val BASE_URL = "http://k11a510.p.ssafy.io:8080/chat-service/"
    private lateinit var tokenManager: TokenManager

    // RetrofitClient 초기화 (앱 시작 시 호출 필요)
    fun initialize(context: Context) {
        tokenManager = TokenManager(context)
    }

    // AuthService 인스턴스 생성
    fun createAuthService(): AuthService {
        // OkHttpClient 설정
        val client = OkHttpClient.Builder()
            // 쿠키 처리를 위한 CookieJar 설정
            .cookieJar(object : CookieJar {
                // 서버로부터 받은 쿠키 저장
                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    // refresh_token 쿠키를 찾아서 TokenManager에 저장
                    cookies.find { it.name == "refresh_token" }?.let { cookie ->
                        tokenManager.saveTokens(
                            accessToken = tokenManager.getAccessToken() ?: "",
                            refreshToken = cookie.value
                        )
                    }
                }

                // 요청 시 저장된 쿠키 불러오기
                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    return tokenManager.getRefreshToken()?.let { refreshToken ->
                        listOf(
                            Cookie.Builder()
                                .name("refresh_token")
                                .value(refreshToken)
                                .domain(url.host)
                                .path("/")
                                .build()
                        )
                    } ?: emptyList()
                }
            })
            // 모든 요청에 토큰을 자동으로 추가하는 인터셉터 설정
            .addInterceptor(TokenInterceptor(tokenManager))
            .build()

        // Retrofit 빌더 설정
        return Retrofit.Builder()
            .baseUrl("https://k11a510.p.ssafy.io/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthService::class.java)
    }

    // AI 채팅 서비스 추가
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val aiChatService: AIChatService = retrofit.create(AIChatService::class.java)
}

//    // 테스트용 가짜 응답
//    fun mockAuthService() = object : AuthService {
//        override suspend fun authenticateKakao(request: KakaoAuthRequest): Response<KakaoAuthResponse> {
//            return Response.success(
//                KakaoAuthResponse(
//                    token = "fake_jwt_token",
//                    userId = "12345",
//                    nickname = "테스트유저"
//                )
//            )
//        }
//    }

    // 가짜 서비스 사용
//    val authService: AuthService = mockAuthService()
//}
