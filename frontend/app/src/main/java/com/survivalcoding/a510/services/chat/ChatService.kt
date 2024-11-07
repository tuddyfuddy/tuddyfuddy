package com.survivalcoding.a510.services.chat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.survivalcoding.a510.repositories.chat.ChatDatabase
import com.survivalcoding.a510.repositories.chat.ChatMessage
import com.survivalcoding.a510.services.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ChatService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val database by lazy { ChatDatabase.getDatabase(applicationContext) }
    private val messageDao by lazy { database.chatMessageDao() }
    private val chatInfoDao by lazy { database.chatInfoDao() }
    private val aiChatService by lazy { RetrofitClient.aiChatService }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_CHAT -> {
                    val roomId = it.getIntExtra(EXTRA_ROOM_ID, -1)
                    val content = it.getStringExtra(EXTRA_CONTENT) ?: return@let
                    val loadingMessageId = if (it.hasExtra(EXTRA_LOADING_MESSAGE_ID)) {
                        it.getLongExtra(EXTRA_LOADING_MESSAGE_ID, -1)
                    } else null
                    handleChatRequest(roomId, content, loadingMessageId)
                }
            }
        }
        return START_STICKY
    }

    private fun handleChatRequest(roomId: Int, content: String, loadingMessageId: Long?) {
        serviceScope.launch {
            try {
                val response = aiChatService.sendChatMessage(
                    // roomId가 5(단톡방)이 아니면 그대로 type에 roomId 보내고, roomId가 5이면... 제작중
                    type = if (roomId != 5) roomId else 1,
                    request = ChatRequest(text = content)
                )

                if (response.isSuccessful) {
                    loadingMessageId?.let { id ->
                        messageDao.deleteMessageById(id)
                    }

                    response.body()?.getMessageList()?.forEach { aiMessage ->
                        messageDao.insertMessage(
                            ChatMessage(
                                roomId = roomId,
                                content = aiMessage,
                                isAiMessage = true
                            )
                        )
                    }

                    response.body()?.getMessageList()?.lastOrNull()?.let { lastAiMessage ->
                        chatInfoDao.updateLastMessage(
                            chatId = roomId,
                            message = lastAiMessage,
                            timestamp = System.currentTimeMillis()
                        )

                        if (activeChatRoomId != roomId) {
                            val currentChat = chatInfoDao.getChatById(roomId)
                            currentChat?.let {
                                chatInfoDao.updateUnreadCount(
                                    chatId = roomId,
                                    count = it.unreadCount + 1
                                )
                            }
                        }
                    }
                } else {
                    // 로딩 메시지 삭제
                    loadingMessageId?.let { id ->
                        messageDao.deleteMessageById(id)
                    }
                    handleError(roomId, "죄송해요, 메시지를 받지 못했어요.")
                }
            } catch (e: Exception) {
                // 로딩 메시지 삭제
                loadingMessageId?.let { id ->
                    messageDao.deleteMessageById(id)
                }
                handleError(roomId, "네트워크 오류가 발생했어요. 인터넷 연결을 확인해주세요.")
            }
        }
    }

    private suspend fun handleError(roomId: Int, errorMessage: String) {
        messageDao.insertMessage(
            ChatMessage(
                roomId = roomId,
                content = errorMessage,
                isAiMessage = true
            )
        )

        chatInfoDao.updateLastMessage(
            chatId = roomId,
            message = errorMessage,
            timestamp = System.currentTimeMillis()
        )

        if (activeChatRoomId != roomId) {
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
                "Chat Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps chat active in background"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("채팅 서비스 실행 중")
        .setContentText("백그라운드에서 메시지를 수신하고 있습니다.")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val CHANNEL_ID = "chat_service_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_START_CHAT = "action_start_chat"
        const val EXTRA_ROOM_ID = "extra_room_id"
        const val EXTRA_CONTENT = "extra_content"
        const val EXTRA_LOADING_MESSAGE_ID = "extra_loading_message_id"
        private var activeChatRoomId: Int? = null  // 현재 사용자 휴대폰 화면이 몇번 채팅방인지 확인하는 변

        fun startService(context: Context, roomId: Int, content: String, loadingMessageId: Long?) {
            val intent = Intent(context, ChatService::class.java).apply {
                action = ACTION_START_CHAT
                putExtra(EXTRA_ROOM_ID, roomId)
                putExtra(EXTRA_CONTENT, content)
                loadingMessageId?.let {
                    putExtra(EXTRA_LOADING_MESSAGE_ID, it)
                }
            }
            context.startForegroundService(intent)
        }
    
        // 현재 화면이 몇번 채팅방인지 확인하기 위함
        fun setActiveChatRoom(roomId: Int?) {
            activeChatRoomId = roomId
        }

        fun getActiveChatRoom(): Int? {
            return activeChatRoomId
        }
    }
}