package com.survivalcoding.a510.services

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FCMTokenService {
    @POST("auth-service/user/fcm-token")
    suspend fun updateFCMToken(
        @Body request: FCMTokenRequest
    ): Response<Unit>
}

data class FCMTokenRequest(
    @SerializedName("fcmToken")
    val token: String
)