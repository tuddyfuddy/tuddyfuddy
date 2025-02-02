package com.survivalcoding.a510.states

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Success(
        val jwtToken: String
    ) : AuthState()
    data class Error(val message: String) : AuthState()
}