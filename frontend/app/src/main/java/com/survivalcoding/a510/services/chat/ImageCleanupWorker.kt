package com.survivalcoding.a510.services.chat

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import com.survivalcoding.a510.repositories.chat.ChatDatabase
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageCleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val messageDao = ChatDatabase.getDatabase(context).chatMessageDao()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 7일 이전 시간 계산
            val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)

            // 이미지 디렉토리 가져오기
            val imageDir = applicationContext.getDir("images", Context.MODE_PRIVATE)

            // 디렉토리에 잇는 모든 이미지 검사
            imageDir.listFiles()?.forEach { file ->
                // 이미지 마지막 수정시간이 7일이 지났는지 체크
                if (file.lastModified() < sevenDaysAgo) {
                    // DB에서 해당 이미지 사용하는 메시지 찾기
                    val messages = messageDao.getMessagesByImageUrl(file.absolutePath)

                    if (messages.isEmpty()) {
                        // DB에서 사용되지 않는 이미지는 삭제
                        file.delete()
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "image_cleanup_work"

        fun schedule(context: Context) {
            val cleanupRequest = PeriodicWorkRequestBuilder<ImageCleanupWorker>(
                1, TimeUnit.DAYS  // 하루 한 번 자동으로 실행시키기
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,  // 이미 예약된 작업이 있다면 유지
                cleanupRequest
            )
        }
    }
}