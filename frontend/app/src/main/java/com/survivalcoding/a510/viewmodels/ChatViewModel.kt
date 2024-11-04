package com.survivalcoding.a510.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.survivalcoding.a510.repositories.chat.ChatDatabase
import com.survivalcoding.a510.repositories.chat.ChatMessage
import com.survivalcoding.a510.services.RetrofitClient
import com.survivalcoding.a510.services.chat.getMessageList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application, private val roomId: Int) : AndroidViewModel(application) {
    private val messageDao = ChatDatabase.getDatabase(application).chatMessageDao()
    private val aiChatService = RetrofitClient.aiChatService

    val allMessages: StateFlow<List<ChatMessage>> = messageDao.getMessagesByRoomId(roomId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun sendMessage(content: String, emotion: String = "평온") {
        viewModelScope.launch {
            // 사용자 메시지 저장
            messageDao.insertMessage(
                ChatMessage(
                    roomId = roomId,
                    content = content,
                    isAiMessage = false
                )
            )

            try {
                // API 호출
                val response = aiChatService.sendChatMessage(
                    type = 1,
                    emotion = emotion,
                    message = content
                )

                if (response.isSuccessful) {
                    // 응답 메시지들을 개별적으로 저장
                    response.body()?.getMessageList()?.forEach { aiMessage ->
                        messageDao.insertMessage(
                            ChatMessage(
                                roomId = roomId,
                                content = aiMessage,
                                isAiMessage = true
                            )
                        )
                    }
                } else {
                    // 에러 처리
                    messageDao.insertMessage(
                        ChatMessage(
                            roomId = roomId,
                            content = "죄송해요, 메시지를 받지 못했어요.",
                            isAiMessage = true
                        )
                    )
                }
            } catch (e: Exception) {
                // 네트워크 에러 등 예외 처리
                messageDao.insertMessage(
                    ChatMessage(
                        roomId = roomId,
                        content = "네트워크 오류가 발생했어요.",
                        isAiMessage = true
                    )
                )
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            messageDao.deleteMessagesByRoomId(roomId)
        }
    }
}