package com.survivalcoding.a510.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Log
import java.io.File
import java.io.IOException
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap

object ImageUtils {
    private const val TAG = "ImageUtils"

    fun loadAndRotateImage(imageFile: File): ImageBitmap? {
        return try {
            // 업로드한 사진에서 exif 정보 가져오기
            val exif = ExifInterface(imageFile.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )

            // 해당 사진의 크기를 확인하ㅓ기
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(imageFile.absolutePath, options)

            // 정해둔 임시 샘플 사이즈로 변경
            val maxSize = 1024
            var sampleSize = 1
            while (options.outWidth / sampleSize > maxSize ||
                options.outHeight / sampleSize > maxSize) {
                sampleSize *= 2
            }

            // 실제로 사용자가 올린 사진 불러오기
            val finalOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath, finalOptions)
                ?: return null

            // 실제 사진 exif 정보에 있는 회전이랑 화면에 보여질 사진 회전 비교하기
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            }

            // 화면에 올릴 사진에 위에서 구한 회전 적용시키기
            val rotatedBitmap = if (!matrix.isIdentity) {
                Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmap.width,
                    bitmap.height,
                    matrix,
                    true
                )
            } else {
                bitmap
            }

            // 이미지비트매븡로 전환하기
            rotatedBitmap.asImageBitmap()

        } catch (e: IOException) {
            Log.e(TAG, "이미지 로드/회전 실패", e)
            null
        }
    }
}