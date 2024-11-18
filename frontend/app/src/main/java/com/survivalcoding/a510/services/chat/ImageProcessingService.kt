package com.survivalcoding.a510.services.chat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.survivalcoding.a510.repositories.chat.ChatDatabase
import com.survivalcoding.a510.repositories.chat.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import android.content.pm.ServiceInfo

class ImageProcessingService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val database by lazy { ChatDatabase.getDatabase(applicationContext) }
    private val messageDao by lazy { database.chatMessageDao() }
    private val chatInfoDao by lazy { database.chatInfoDao() }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_PROCESS_IMAGE -> {
                    val roomId = it.getIntExtra(EXTRA_ROOM_ID, -1)
                    val imageUri = it.getParcelableExtra<Uri>(EXTRA_IMAGE_URI)
                    if (roomId != -1 && imageUri != null) {
                        serviceScope.launch {
                            handleImageProcessing(roomId, imageUri)
                        }
                    }
                }
            }
        }
        return START_STICKY
    }

    private suspend fun handleImageProcessing(roomId: Int, imageUri: Uri) {
        try {
            // 1. 이미지 저장
            // imageUri가 알려주는 이미지를 내부저장소에 저장하고, 저장경로 반환
            val savedImagePath = saveImageToInternalStorage(imageUri)
            Log.d("ImageProcessing", "저장한 이미지 파일 이름: $savedImagePath")

            // 2. 이미지 메시지 저장
            // RoomDB에도 해당 이미지 저장
            val imageMessageId = messageDao.insertMessageAndGetId(
                ChatMessage(
                    roomId = roomId,
                    content = "",
                    isAiMessage = false,
                    isImage = true,
                    imageUrl = savedImagePath
                )
            )
            Log.d("ImageProcessing", "저장한 이미지 메시지 ID: $imageMessageId")

            // 3. 이미지 분석 API 호출
            // ImageService를 호출하여 이미지를 분석 API로 보낸 후 분석 결과를 받음
            val response = ImageService.uploadAndAnalyzeImage(applicationContext, imageUri)
            if (response.isSuccessful && response.body() != null) {
                val result = response.body()!!.result

                // Flow를 통해 메시지 전달
                pendingMessageCallback.value = Pair(roomId, "[[사진]${result.description}]")
            }
        } catch (e: Exception) {
            handleError(roomId, "이미지 처리 중 오류가 발생했습니다.\n${e.message}")
        }
    }

    // 이미지를 로컬 저장소에 저장하는 함수
    private fun saveImageToInternalStorage(uri: Uri): String {
        // Uri로부터 입력 스트림을 열어 이미지 데이터를 읽어 온다
        val inputStream = applicationContext.contentResolver.openInputStream(uri)

        // 현재 시간을 기준으로 타임스탬프 변수를 생성
        val timestamp = System.currentTimeMillis()

        // 이미지 파일 이름을 "image_타임스탬프.jpg" 형식으로 설정
        val filename = "image_$timestamp.jpg"

        // 내부 저장소에 images 디렉토리를 생성(이미 있으면 기존 디렉토리 사용)
        val directory = applicationContext.getDir("images", Context.MODE_PRIVATE)

        // 저장될 파일의 경로와 이름을 설정
        val file = File(directory, filename)

        Log.d("ImageProcessing", "저장한 이미지 파일 이름: $filename")

        // 입력 스트림으로부터 데이터를 읽어 파일에 복사
        applicationContext.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output) // input 스트림의 데이터를 output 스트림으로 복사
            }
        }

        // 저장된 파일의 이름을 반환
        return filename
    }

    // 에러 메시지를 채팅방에 저장하는 함수
    private suspend fun handleError(roomId: Int, errorMessage: String) {
        // 에러 메시지를 채팅 메시지로 저장
        messageDao.insertMessage(
            ChatMessage(
                roomId = roomId,
                content = errorMessage,
                isAiMessage = true
            )
        )

        // 현재 사용자 화면에 보고 있는 채팅방이 아니면 읽지 않은 메시지 증가
        if (ChatService.getActiveChatRoom() != roomId) {
            // 현재 채팅방 정보 가져오기
            val currentChat = chatInfoDao.getChatById(roomId)
            currentChat?.let {
                // 읽지 않은 메시지 개수를 증가
                chatInfoDao.updateUnreadCount(
                    chatId = roomId,
                    count = it.unreadCount + 1 // 기존 읽지 않은 메시지 수에 1을 추가
                )
            }
        }
    }

    // 알림 채널을 생성하는 함수
    private fun createNotificationChannel() {
        // Android8.0 버전 이상에서만 알림 채널을 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, // 채널 아이디
                "Silent Channel",     // 채널 이름
                NotificationManager.IMPORTANCE_MIN    // 채널 중요도 낮음
            ).apply {
                description = ""
                setShowBadge(false)
                enableLights(false) // LED 끄기
                enableVibration(false) // 진동 끄기
                setSound(null, null) // 알림음 없애기
            }

            // NotificationManager를 통해 채널을 등록
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // 알림을 생성하는 함수
    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("") // 빈 제목
        .setContentText("")  // 빈 내용
        .setSmallIcon(android.R.drawable.ic_menu_gallery)
        .setPriority(NotificationCompat.PRIORITY_MIN) // 최소 우선순위로 설정
        .build()                                       // 알림 생성

    // onBind 함수: 서비스가 바인딩되지 않도록 null 반환
    override fun onBind(intent: Intent?): IBinder? = null

    // 서비스가 종료될 때 호출되는 함수
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val CHANNEL_ID = "image_processing_channel" // 알림 채널 ID를 정의
        private const val NOTIFICATION_ID = 2                     // 알림 ID를 정의
        const val ACTION_PROCESS_IMAGE = "action_process_image"   // 이미지 처리 액션 상수 정의
        // 방 ID와 이미지 URI를 추가 데이터로 전달하기 위한 상수 정의
        const val EXTRA_ROOM_ID = "extra_room_id"
        const val EXTRA_IMAGE_URI = "extra_image_uri"

        // 메시지 대기열을 관리하기 위한 MutableStateFlow
        private val pendingMessageCallback = MutableStateFlow<Pair<Int, String>?>(null)

        // 서비스 시작 함수
        fun startService(context: Context, roomId: Int, imageUri: Uri) {
            // 인텐트를 생성하고 필요한 데이터를 담아 ImageProcessingService를 시작
            val intent = Intent(context, ImageProcessingService::class.java).apply {
                action = ACTION_PROCESS_IMAGE
                putExtra(EXTRA_ROOM_ID, roomId)
                putExtra(EXTRA_IMAGE_URI, imageUri)
            }
            context.startForegroundService(intent)  // 백그라운드에서 시작
        }

        // ViewModel에서 구독할 Flow
        fun getPendingMessageFlow(): StateFlow<Pair<Int, String>?> = pendingMessageCallback.asStateFlow()

        // 대기 중인 메세지 콜백을 초기화하는 함수
        fun clearPendingMessage() {
            pendingMessageCallback.value = null
        }
    }
}