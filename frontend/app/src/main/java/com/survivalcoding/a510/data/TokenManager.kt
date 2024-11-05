package com.survivalcoding.a510.data

import android.content.Context

// 토큰을 저장하고 관리하는 클래스
class TokenManager(context: Context) {
    // 앱의 SharedPreferences 인스턴스를 생성합니다.
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)


    // Access Token과 Refresh Token을 저장합니다.
    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
    }

    fun getAccessToken(): String? {
        return prefs.getString("access_token", null)
    }

    fun getRefreshToken(): String? {
        return prefs.getString("refresh_token", null)
    }

    // 로그아웃시 사용됨
    fun clearTokens() {
        prefs.edit().clear().apply()
    }
}