package com.survivalcoding.a510.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.survivalcoding.a510.repositories.chat.ChatDatabase
import com.survivalcoding.a510.repositories.chat.ChatMessage
import com.survivalcoding.a510.services.RetrofitClient
import com.survivalcoding.a510.services.chat.ChatRequest
import com.survivalcoding.a510.services.chat.ChatService
import com.survivalcoding.a510.services.chat.getMessageList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application, private val roomId: Int) : AndroidViewModel(application) {
    private val database = ChatDatabase.getDatabase(application)
    private val messageDao = database.chatMessageDao()
    private val chatInfoDao = database.chatInfoDao()
    private val aiChatService = RetrofitClient.aiChatService

    val allMessages: StateFlow<List<ChatMessage>> = messageDao.getMessagesByRoomId(roomId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun sendMessage(content: String) {
        viewModelScope.launch {
            // 사용자 메시지 저장
            messageDao.insertMessage(
                ChatMessage(
                    roomId = roomId,
                    content = content,
                    isAiMessage = false
                )
            )

            // 채팅방 정보 업데이트 (마지막 메시지)
            chatInfoDao.updateLastMessage(
                chatId = roomId,
                message = content,
                timestamp = System.currentTimeMillis()
            )

            // Foreground 서비스 시작
            ChatService.startService(getApplication(), roomId, content)
        }
    }

    // 채팅방 들어가면 읽지 않은 메세지 수 다시 0으로 초기화
    fun markAsRead() {
        viewModelScope.launch {
            chatInfoDao.updateUnreadCount(roomId, 0)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            // 선택한 채팅방의 메시지만 전체 삭제
            messageDao.deleteMessagesByRoomId(roomId)

            chatInfoDao.updateLastMessage(
                chatId = roomId,
                message = "친구와 새로운 대화를 시작해보세요!",
                timestamp = System.currentTimeMillis()
            )
        }
    }}