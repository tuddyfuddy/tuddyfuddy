package com.survivalcoding.a510.viewmodels

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.survivalcoding.a510.services.KakaoAuthRequest
import com.survivalcoding.a510.services.RetrofitClient
import com.survivalcoding.a510.states.AuthState
import com.survivalcoding.a510.utils.TokenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {  // Context 제거
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var tokenRepository: TokenRepository? = null  // null 허용

    fun initializeTokenRepository(activity: ComponentActivity) {
        if (tokenRepository == null) {
            tokenRepository = TokenRepository(activity)
            checkLoginStatus()
        }
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            val token = tokenRepository?.getToken()
            _authState.value = if (token != null) {
                AuthState.Success(null, null, token)
            } else {
                AuthState.Initial
            }
        }
    }

    fun handleKakaoLogin(
        activity: ComponentActivity,  // Activity를 매개변수로 받음
        onSuccess: () -> Unit,
        onError: (error: Throwable) -> Unit
    ) {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                handleKakaoLoginResult(token, error, onSuccess, onError)
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(activity) { token, error ->
                handleKakaoLoginResult(token, error, onSuccess, onError)
            }
        }
    }

    private fun handleKakaoLoginResult(
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

                    // 테스트를 위해 서버 호출 대신 즉시 성공 처리
                    _authState.value = AuthState.Success(
                        kakaoUserId = null,
                        nickname = "테스트유저",
                        jwtToken = token.accessToken  // 카카오 토큰을 임시로 사용
                    )
                    onSuccess()

                    /* 실제 서버 연동시 사용할 코드
                    val response = RetrofitClient.authService.authenticateKakao(
                        KakaoAuthRequest(token.accessToken)
                    )

                    if (response.isSuccessful) {
                        response.body()?.let { authResponse ->
                            tokenRepository?.saveToken(authResponse.token)

                            _authState.value = AuthState.Success(
                                kakaoUserId = null,
                                nickname = authResponse.nickname,
                                jwtToken = authResponse.token
                            )
                            onSuccess()
                        }
                    } else {
                        _authState.value = AuthState.Error("Server authentication failed")
                        onError(Exception("Server authentication failed"))
                    }
                    */
                } catch (e: Exception) {
                    _authState.value = AuthState.Error(e.message ?: "Network error")
                    onError(e)
                }
            }
        }
    }
}