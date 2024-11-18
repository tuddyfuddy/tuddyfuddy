package com.survivalcoding.a510.services.chat

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.survivalcoding.a510.services.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

// 파일 종류를 확인하는 확장 함수
fun Context.getFileType(uri: Uri): String? {
    val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
}

data class ImageAnalysisResponse(
    val statusCode: Int,
    val message: String,
    val result: ImageAnalysisResult
)

data class ImageAnalysisResult(
    val description: String,
    val imageUrl: String
)

interface ImageAnalysisService {
    @Multipart
    @POST("images/analysis")
    suspend fun analyzeImage(
        @Part image: MultipartBody.Part
    ): Response<ImageAnalysisResponse>
}

object ImageService {
    // 1MB로 제한 낮춤
    private const val MAX_FILE_SIZE = 1 * 1024 * 1024 // 1MB
    private const val TAG = "ImageService"

    private fun compressImage(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)

        Log.d(TAG, "Original image size: ${originalBitmap.byteCount} bytes")

        // 비트맵 크기가 너무 크면 리사이징
        val maxDimension = 1024  // 최대 1024x1024
        val scaledBitmap = if (originalBitmap.width > maxDimension || originalBitmap.height > maxDimension) {
            val ratio = maxDimension.toFloat() / maxOf(originalBitmap.width, originalBitmap.height)
            Bitmap.createScaledBitmap(
                originalBitmap,
                (originalBitmap.width * ratio).toInt(),
                (originalBitmap.height * ratio).toInt(),
                true
            )
        } else {
            originalBitmap
        }

        Log.d(TAG, "After resize: ${scaledBitmap.width}x${scaledBitmap.height}, ${scaledBitmap.byteCount} bytes")

        var quality = 100
        val outputStream = ByteArrayOutputStream()

        // 압축 시작
        do {
            outputStream.reset()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            Log.d(TAG, "Compressed with quality $quality, size: ${outputStream.size()} bytes")
            quality -= 20  // 더 많이 압축하기 위해 10씩 감소
        } while (outputStream.size() > MAX_FILE_SIZE && quality > 10)

        if (quality <= 10) {
            Log.w(TAG, "Warning: Image compressed to minimum quality but still exceeds size limit")
        }

        // 압축된 이미지를 임시 파일로 저장
        val tempFile = File.createTempFile("compressed_image", ".jpg", context.cacheDir)
        FileOutputStream(tempFile).use { fos ->
            fos.write(outputStream.toByteArray())
        }

        Log.d(TAG, "Final file size: ${tempFile.length()} bytes")

        // 원본 비트맵 메모리 해제
        if (originalBitmap != scaledBitmap) {
            originalBitmap.recycle()
        }
        if (scaledBitmap != originalBitmap) {
            scaledBitmap.recycle()
        }

        return tempFile
    }

    suspend fun uploadAndAnalyzeImage(context: Context, imageUri: Uri): Response<ImageAnalysisResponse> {
        try {
            val mimeType = context.getFileType(imageUri) ?: "image/jpeg"
            Log.d(TAG, "File mime type: $mimeType")

            val compressedFile = compressImage(context, imageUri)
            Log.d(TAG, "Compressed file size: ${compressedFile.length()} bytes")

            if (compressedFile.length() > MAX_FILE_SIZE) {
                throw IllegalStateException("File size exceeds limit even after compression")
            }

            val requestBody = compressedFile.asRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("image", compressedFile.name, requestBody)

            return RetrofitClient.imageAnalysisService.analyzeImage(part)
        } catch (e: Exception) {
            Log.e(TAG, "Error during image upload", e)
            throw e
        }
    }
}