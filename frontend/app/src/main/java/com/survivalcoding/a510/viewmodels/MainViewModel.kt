package com.survivalcoding.a510.viewmodels

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.survivalcoding.a510.data.TokenManager
import com.survivalcoding.a510.services.KakaoAuthRequest
import com.survivalcoding.a510.services.RetrofitClient
import com.survivalcoding.a510.states.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


// 메인 화면의 인증 관련 로직을 처리하는 ViewModel
class MainViewModel : ViewModel() {
    // 인증 상태를 관리하는 StateFlow
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // 토큰 관리자 인스턴스
    private var tokenManager: TokenManager? = null
    // 인증 서비스 인스턴스 (지연 초기화)
    private val authService by lazy { RetrofitClient.createAuthService() }

    /**
     * ViewModel 초기화 및 토큰 매니저 설정
     * @param activity 토큰 저장에 사용될 Context를 제공하는 Activity
     */
    fun initialize(activity: ComponentActivity) {
        if (tokenManager == null) {
            tokenManager = TokenManager(activity)
            checkLoginStatus()
        }
    }

    /**
     * 저장된 토큰을 확인하여 로그인 상태를 체크
     */
    private fun checkLoginStatus() {
        viewModelScope.launch {
            val accessToken = tokenManager?.getAccessToken()
            _authState.value = if (accessToken != null) {
                AuthState.Success(null, null, accessToken)
            } else {
                AuthState.Initial
            }
        }
    }

    /**
     * 카카오 로그인 처리
     * @param activity 카카오 로그인에 필요한 Activity
     * @param onSuccess 로그인 성공 시 콜백
     * @param onError 로그인 실패 시 콜백
     */
    fun handleKakaoLogin(
        activity: ComponentActivity,
        onSuccess: () -> Unit,
        onError: (error: Throwable) -> Unit
    ) {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            // 카카오톡이 설치되어 있으면 카카오톡으로 로그인
            UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                handleKakaoLoginResult(activity, token, error, onSuccess, onError)
            }
        } else {
            // 카카오톡이 설치되어 있지 않으면 카카오계정으로 로그인
            UserApiClient.instance.loginWithKakaoAccount(activity) { token, error ->
                handleKakaoLoginResult(activity, token, error, onSuccess, onError)
            }
        }
    }

    /**
     * 카카오 로그인 결과 처리
     * @param token 카카오 인증 토큰
     * @param error 발생한 에러
     * @param onSuccess 성공 콜백
     * @param onError 에러 콜백
     */
    private fun handleKakaoLoginResult(
        activity: ComponentActivity,
        token: OAuthToken?,
        error: Throwable?,
        onSuccess: () -> Unit,
        onError: (error: Throwable) -> Unit
    ) {
        if (error != null) {
            _authState.value = AuthState.Error(error.message ?: "Unknown error")
            onError(error)
            return
        }

        if (token != null) {
            viewModelScope.launch {
                try {
                    _authState.value = AuthState.Loading

                    // 서버에 카카오 토큰으로 인증 요청
                    val response = authService.authenticateKakao(
                        KakaoAuthRequest(token.accessToken)
                    )

                    if (response.isSuccessful) {
                        // 서버 응답에서 Authorization 헤더 추출
                        val accessToken = response.headers()["Authorization"]
                            ?.removePrefix("Bearer ")

//                        // Access Token 로그 출력
//                        Log.d("MainViewModel", "Access Token: $accessToken")
//                        // 전체 헤더 로그 출력
//                        Log.d("MainViewModel", "All Headers: ${response.headers()}")

                        if (accessToken != null) {
                            // TokenManager에 저장 (refresh token은 자동으로 저장됨)
                            tokenManager?.saveTokens(accessToken, "")

                            // 응답 바디에서 사용자 정보 추출
                            response.body()?.let { authResponse ->
                                _authState.value = AuthState.Success(
                                    kakaoUserId = null,
                                    nickname = authResponse.nickname,
                                    jwtToken = accessToken
                                )
                                onSuccess()
                            }
                        } else {
                            _authState.value = AuthState.Error("No access token in response")
                            onError(Exception("No access token in response"))
                        }
                    } else {
                        _authState.value = AuthState.Error("Server authentication failed")
                        onError(Exception("Server authentication failed"))
                    }
                } catch (e: Exception) {
                    _authState.value = AuthState.Error(e.message ?: "Network error")
                    onError(e)
                }
            }
        }
    }

    /**
     * 로그아웃 처리
     * 저장된 토큰을 삭제하고 초기 상태로 되돌림
     */
    fun logout() {
        tokenManager?.clearTokens()
        _authState.value = AuthState.Initial
    }
}