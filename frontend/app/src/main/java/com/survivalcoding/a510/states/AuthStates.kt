package com.survivalcoding.a510.states

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Success(
        val userId: Long?,
        val nickname: String?
    ) : AuthState()
    data class Error(val message: String) : AuthState()
}