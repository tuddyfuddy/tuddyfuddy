package com.survivalcoding.a510.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        _isLoggedIn.value = AuthApiClient.instance.hasToken()
    }

    fun handleKakaoLogin(
        context: android.content.Context,
        onSuccess: () -> Unit,
        onError: (error: Throwable) -> Unit
    ) {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            // 카카오톡 로그인
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                handleLoginResult(token, error, onSuccess, onError)
            }
        } else {
            // 카카오계정 로그인
            UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
                handleLoginResult(token, error, onSuccess, onError)
            }
        }
    }

    private fun handleLoginResult(
        token: OAuthToken?,
        error: Throwable?,
        onSuccess: () -> Unit,
        onError: (error: Throwable) -> Unit
    ) {
        if (error != null) {
            onError(error)
        } else if (token != null) {
            viewModelScope.launch {
                _isLoggedIn.value = true
                onSuccess()
            }
        }
    }
}