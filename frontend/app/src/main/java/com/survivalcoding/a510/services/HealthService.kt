package com.survivalcoding.a510.services

import android.util.Log
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

private const val TAG = "A510HealthService"

data class HealthRequest(
    val heartRate: Int,
    val steps: Int,
    val sleepMinutes: Int,
    val stressLevel: Int
)

data class HealthResponse(
    val statusCode: Int,
    val message: String,
    val result: HealthData
)

data class HealthData(
    val heartRate: Int,
    val steps: Int,
    val sleepMinutes: Int,
    val stressLevel: Int,
    val createdAt: String,
    val updatedAt: String
)

interface HealthService {
    @POST("/context/health")
    suspend fun sendHealthData(
        @Body request: HealthRequest
    ): Response<HealthResponse>
}

// 로그 찍어보는 함수 
suspend fun HealthService.sendHealthDataWithLogging(request: HealthRequest): Response<HealthResponse> {
    // 내가 보낸 데이터 로그
    Log.d(TAG, """
        Sending health data request:
        - Heart Rate: ${request.heartRate}
        - Steps: ${request.steps}
        - Sleep Minutes: ${request.sleepMinutes}
        - Stress Level: ${request.stressLevel}
    """.trimIndent())

    // API 호출
    val response = sendHealthData(request)

    // 응답 성공하면 응답온거 로그
    if (response.isSuccessful) {
        response.body()?.let { responseBody ->
            Log.d(TAG, """
                Health data response received:
                Status Code: ${responseBody.statusCode}
                Message: ${responseBody.message}
                Result:
                - Heart Rate: ${responseBody.result.heartRate}
                - Steps: ${responseBody.result.steps}
                - Sleep Minutes: ${responseBody.result.sleepMinutes}
                - Stress Level: ${responseBody.result.stressLevel}
                - Created At: ${responseBody.result.createdAt}
                - Updated At: ${responseBody.result.updatedAt}
            """.trimIndent())
        }
    } else {
        // 응답 실패하면 에러메세지 로그
        Log.e(TAG, """
            Error response:
            Code: ${response.code()}
            Error Body: ${response.errorBody()?.string()}
            Headers: ${response.headers()}
        """.trimIndent())
    }

    return response
}