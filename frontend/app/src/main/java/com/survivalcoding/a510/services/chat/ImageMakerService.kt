package com.survivalcoding.a510.services.chat

import android.util.Log
import com.survivalcoding.a510.services.RetrofitClient
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class ImageGenerationRequest(
    val text: String
)

data class ImageGenerationResponse(
    val statusCode: Int,
    val message: String,
    val result: String
)

interface ImageGenerationService {
    @POST("images/ai/gpt")
    suspend fun generateImage(
        @Body request: ImageGenerationRequest
    ): Response<ImageGenerationResponse>
}

object ImageMakerService {
    private const val TAG = "ImageMakerService"

    suspend fun generateImage(prompt: String): Response<ImageGenerationResponse> {
        return try {
            val request = ImageGenerationRequest(text = prompt)
            Log.d(TAG, "API에 보내는 프롬포트: $prompt")

            RetrofitClient.imageGenerationService.generateImage(request)
        } catch (e: Exception) {
            Log.e(TAG, "이미지 생성에 실패함", e)
            throw e
        }
    }
}