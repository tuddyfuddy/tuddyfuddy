package com.survivalcoding.a510.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.survivalcoding.a510.MainActivity
import com.survivalcoding.a510.R
import com.survivalcoding.a510.data.TokenManager
import com.survivalcoding.a510.repositories.chat.ChatDatabase
import com.survivalcoding.a510.repositories.chat.ChatMessage
import com.survivalcoding.a510.services.chat.ChatService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private lateinit var tokenManager: TokenManager

    override fun onCreate() {
        super.onCreate()
        // TokenManager 초기화
        tokenManager = TokenManager(this)
    }

    // 1. FCM 토큰이 갱신될 때 호출되어 로컬에만 저장
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // 새 토큰을 로컬에만 저장
        tokenManager.saveFCMToken(token)
        Log.d(TAG, "New FCM token saved locally")
    }

    // 2. 푸시 메시지를 수신했을 때 호출
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCMcccc message received: ${remoteMessage.data}")

        // 수신된 메시지에 데이터 페이로드가 포함되어 있는 경우
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // 수신된 메시지에 알림 페이로드가 포함되어 있는 경우
        remoteMessage.notification?.let {
            Log.d(TAG, "Message notification payload: ${it.title} - ${it.body}")
            sendNotification(it.title, it.body, remoteMessage.data)
        }
    }

    // 4. 데이터 메시지 처리
    private fun handleDataMessage(data: Map<String, String>) {
        Log.d(TAG, "💬 Data Message: $data")
        Log.d(TAG, "\n=== Data Message ===\n${data.entries.joinToString("\n")}\n==================")

        // 메시지 타입 뭔지 확인하고
        val messageType = data["messageType"]

        // 메세지 타입이 웨더이거나 캘린더일때만 RoomDB에 저장하
        if (messageType == "WEATHER" || messageType == "CALENDAR") {
            val roomId = data["roomId"]?.toIntOrNull()
            val message = data["message"]
            val aiName = data["aiName"]

            if (roomId != null && message != null) {
                // RoomDB 초기화
                val database = ChatDatabase.getDatabase(applicationContext)
                val messageDao = database.chatMessageDao()
                val chatInfoDao = database.chatInfoDao()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // <br> 기준으로 메시지 분리시키기
                        val messages = message.split("<br>")

                        // 각 메시지를 개별적으로 저장
                        messages.forEach { splitMessage ->
                            if (splitMessage.isNotBlank()) {
                                messageDao.insertMessage(
                                    ChatMessage(
                                        roomId = roomId,
                                        content = splitMessage.trim(),
                                        isAiMessage = true,
                                        aiType = when(aiName) {
                                            "Fuddy" -> 2
                                            "Buddy" -> 3
                                            "Study" -> 4
                                            else -> null
                                        }
                                    )
                                )
                                delay(100)
                            }
                        }

                        // 채팅방 목록 정보 업데이트
                        chatInfoDao.updateLastMessage(
                            chatId = roomId,
                            message = message.replace("<br>", " "),
                            timestamp = System.currentTimeMillis()
                        )

                        // 현재 활성화된 채팅방 ID 가져오기
                        val currentActiveChatRoom = ChatService.getActiveChatRoom()

                        // 현재 사용자가 해당 채팅방을 보고있지 않다면 읽지 않은 메시지 수 증가
                        if (currentActiveChatRoom != roomId) {
                            val currentChat = chatInfoDao.getChatById(roomId)
                            currentChat?.let {
                                chatInfoDao.updateUnreadCount(
                                    chatId = roomId,
                                    count = it.unreadCount + 1
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving message to database", e)
                    }
                }
            }
        }
        // CHAT 타입은 무시 (이미 ChatService에서 처리됨)
    }

    // 5. 알림 생성 및 표시
    private fun sendNotification(title: String?, messageBody: String?, data: Map<String, String>) {
        // 푸시 알림 데이터에서 채팅방 ID 찾기
        val chatRoomId = data["roomId"]?.toIntOrNull()

        // 현재 사용자 화면에 떠있는 채팅방 ID 찾기
        val currentChatRoomId = ChatService.getActiveChatRoom()

        // 현재 사용자 화면과 같은 roomId라면 푸시알림 안보내기
        if (chatRoomId != null && chatRoomId == currentChatRoomId) {
            return
        }
        
        val channelId = "default_channel"

        // 알림 클릭시 실행될 액티비티 설정
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // 알림 스타일 설정
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification_logo)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android Oreo 이상에서는 채널 생성이 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 표시
        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "FCMService"
    }
}