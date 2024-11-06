package com.survivalcoding.a510.services.chat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.survivalcoding.a510.repositories.chat.ChatDatabase
import com.survivalcoding.a510.repositories.chat.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ImageProcessingService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val database by lazy { ChatDatabase.getDatabase(applicationContext) }
    private val messageDao by lazy { database.chatMessageDao() }
    private val chatInfoDao by lazy { database.chatInfoDao() }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_PROCESS_IMAGE -> {
                    val roomId = it.getIntExtra(EXTRA_ROOM_ID, -1)
                    val imageUri = it.getParcelableExtra<Uri>(EXTRA_IMAGE_URI)
                    if (roomId != -1 && imageUri != null) {
                        handleImageProcessing(roomId, imageUri)
                    }
                }
            }
        }
        return START_STICKY
    }

    private fun handleImageProcessing(roomId: Int, imageUri: Uri) {
        serviceScope.launch {
            try {
                // 사용자가 보낸 이미지 로컬 저장소로 복사
                val savedImagePath = saveImageToInternalStorage(imageUri)

                // 이미지 분석 API로 업로드하기
                val response = ImageService.uploadAndAnalyzeImage(applicationContext, imageUri)

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!.result

                    // 기존 메시지의 imageUrl을 로컬 저장소 경로로 변경
                    messageDao.updateLastMessageImageUrl(roomId, savedImagePath)

                    // 이미지 분석 API 결과 전달
                    ChatService.startService(
                        applicationContext,
                        roomId,
                        "사용자가 이미지를 공유했습니다.\n이미지 URL: ${result.imageUrl}\n이미지 설명: ${result.description}"
                    )
                } else {
                    handleError(roomId, "이미지 처리 중 오류가 발생했습니다. (Error: ${response.code()})")
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is OutOfMemoryError -> "이미지가 너무 큽니다."
                    is SecurityException -> "이미지 접근 권한이 없습니다."
                    is IllegalArgumentException -> "올바르지 않은 이미지 파일입니다."
                    else -> "이미지 처리 중 오류가 발생했습니다.\n${e.message}"
                }
                handleError(roomId, errorMessage)
            }
        }
    }

    // 이미지를 로컬 저장소에 저장하는 함수
    private fun saveImageToInternalStorage(uri: Uri): String {
        val inputStream = applicationContext.contentResolver.openInputStream(uri)
        val timestamp = System.currentTimeMillis()
        val filename = "image_$timestamp.jpg"
        val directory = applicationContext.getDir("images", Context.MODE_PRIVATE)
        val file = File(directory, filename)

        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        return file.absolutePath
    }

    private suspend fun handleError(roomId: Int, errorMessage: String) {
        messageDao.insertMessage(
            ChatMessage(
                roomId = roomId,
                content = errorMessage,
                isAiMessage = true
            )
        )

        if (ChatService.getActiveChatRoom() != roomId) {
            val currentChat = chatInfoDao.getChatById(roomId)
            currentChat?.let {
                chatInfoDao.updateUnreadCount(
                    chatId = roomId,
                    count = it.unreadCount + 1
                )
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Image Processing Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Processes images in background"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("이미지 처리 중")
        .setContentText("백그라운드에서 이미지를 처리하고 있습니다.")
        .setSmallIcon(android.R.drawable.ic_menu_gallery)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val CHANNEL_ID = "image_processing_channel"
        private const val NOTIFICATION_ID = 2
        const val ACTION_PROCESS_IMAGE = "action_process_image"
        const val EXTRA_ROOM_ID = "extra_room_id"
        const val EXTRA_IMAGE_URI = "extra_image_uri"

        fun startService(context: Context, roomId: Int, imageUri: Uri) {
            val intent = Intent(context, ImageProcessingService::class.java).apply {
                action = ACTION_PROCESS_IMAGE
                putExtra(EXTRA_ROOM_ID, roomId)
                putExtra(EXTRA_IMAGE_URI, imageUri)
            }
            context.startForegroundService(intent)
        }
    }
}