package com.survivalcoding.a510.services

import android.content.Context
import com.survivalcoding.a510.data.TokenManager
import com.survivalcoding.a510.services.chat.AIChatService
import com.survivalcoding.a510.services.chat.ImageAnalysisService
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Retrofit 인스턴스를 관리하는 싱글톤 객체
object RetrofitClient {
    private const val CHAT_BASE_URL = "http://k11a510.p.ssafy.io:8080/chat-service/"
    private const val IMAGE_BASE_URL = "http://k11a510.p.ssafy.io:8080/"
    private const val BASE_URL = "http://k11a510.p.ssafy.io:8080/"
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
            .baseUrl("http://k11a510.p.ssafy.io:8080/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthService::class.java)
    }

    // 공통 OkHttpClient를 생성하는 private 함수
    private fun createClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor(tokenManager))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // 채팅 서비스용 Retrofit 인스턴스
    private val chatRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(CHAT_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(createClient())
            .build()
    }

    // 이미지 분석용 Retrofit 인스턴스
    private val imageRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(IMAGE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(createClient())
            .build()
    }

    // Weather 서비스용 Retrofit 인스턴스
    private val weatherRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(createClient())
            .build()
    }

    // Calendar 서비스용 Retrofit 인스턴스
    private val calendarRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(createClient())
            .build()
    }

    // FCM 토큰 업데이트를 위한 서비스
    fun createFCMTokenService(): FCMTokenService {
        val client = OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor(tokenManager))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .baseUrl("http://k11a510.p.ssafy.io:8080/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FCMTokenService::class.java)
    }

    // 채팅 서비스
    val aiChatService: AIChatService by lazy {
        chatRetrofit.create(AIChatService::class.java)
    }
    // 이미지 분석 서비스
    val imageAnalysisService: ImageAnalysisService by lazy {
        imageRetrofit.create(ImageAnalysisService::class.java)
    }

    // Weather 서비스
    val weatherService: WeatherService by lazy {
        weatherRetrofit.create(WeatherService::class.java)
    }

    // Calendar 서비스
    val calendarService: CalendarService by lazy {
        calendarRetrofit.create(CalendarService::class.java)
    }

}

