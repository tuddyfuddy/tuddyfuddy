package com.survivalcoding.a510.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.survivalcoding.a510.MainActivity
import com.survivalcoding.a510.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "chat_notifications"
        private const val CHANNEL_NAME = "Chat Notifications"
        private const val NOTIFICATION_ID = 1
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, """
            New FCM Token Generated:
            token: $token
            timestamp: ${System.currentTimeMillis()}
        """.trimIndent())

        // TODO: FCM 토큰을 서버에 전송하는 로직 구현
        sendTokenToServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, """
            FCM Message Received:
            from: ${message.from}
            data: ${message.data}
            notification: ${message.notification?.body}
        """.trimIndent())

        // 메시지 종류에 따른 처리
        when {
            message.data.isNotEmpty() -> handleDataMessage(message.data)
            message.notification != null -> handleNotificationMessage(message.notification!!)
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        // 데이터 메시지 처리 로직
        val messageType = data["type"]
        val content = data["content"]
        val senderId = data["senderId"]

        Log.d(TAG, """
            Data Message Details:
            type: $messageType
            content: $content
            senderId: $senderId
        """.trimIndent())

        // 데이터 타입에 따른 알림 생성
        when (messageType) {
            "chat" -> showNotification(
                title = "New Message",
                message = content ?: "You received a new message"
            )
            "invite" -> showNotification(
                title = "New Invitation",
                message = content ?: "You received a new invitation"
            )
            else -> showNotification(
                title = "New Notification",
                message = content ?: "You received a new notification"
            )
        }
    }

    private fun handleNotificationMessage(notification: RemoteMessage.Notification) {
        showNotification(
            title = notification.title ?: "New Message",
            message = notification.body ?: "You received a new message"
        )
    }

    private fun showNotification(title: String, message: String) {
        // MainActivity를 시작하는 Intent 생성
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        // PendingIntent 생성
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // 알림 소리 설정
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // 알림 빌더 설정
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // 앱의 실제 아이콘으로 변경 필요
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 안드로이드 Oreo 이상에서는 채널 생성이 필수
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Receives chat and other important notifications"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 표시
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun sendTokenToServer(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Sending FCM token to server: $token")

                val fcmService = RetrofitClient.createFCMTokenService()
                val response = fcmService.updateFCMToken(FCMTokenRequest(token))

                if (response.isSuccessful) {
                    Log.d(TAG, "Successfully sent FCM token to server")
                } else {
                    Log.e(TAG, """
                    Failed to send FCM token to server:
                    Response code: ${response.code()}
                    Error body: ${response.errorBody()?.string()}
                """.trimIndent())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending FCM token to server", e)
            }
        }
    }
}