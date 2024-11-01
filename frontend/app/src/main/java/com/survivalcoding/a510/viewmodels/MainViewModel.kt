package com.survivalcoding.a510.viewmodels

import android.content.Context
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

class MainViewModel(private val context: Context) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val tokenRepository = TokenRepository(context)

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            val token = tokenRepository.getToken()
            _authState.value = if (token != null) {
                AuthState.Success(null, null, token)
            } else {
                AuthState.Initial
            }
        }
    }

    fun handleKakaoLogin(
        onSuccess: () -> Unit,
        onError: (error: Throwable) -> Unit
    ) {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                handleKakaoLoginResult(token, error, onSuccess, onError)
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
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

                    val response = RetrofitClient.authService.authenticateKakao(
                        KakaoAuthRequest(token.accessToken)
                    )

                    if (response.isSuccessful) {
                        response.body()?.let { authResponse ->
                            tokenRepository.saveToken(authResponse.token)

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
                } catch (e: Exception) {
                    _authState.value = AuthState.Error(e.message ?: "Network error")
                    onError(e)
                }
            }
        }
    }
}