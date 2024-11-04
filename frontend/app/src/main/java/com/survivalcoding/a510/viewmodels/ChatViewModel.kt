package com.survivalcoding.a510.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.survivalcoding.a510.repositories.chat.ChatDatabase
import com.survivalcoding.a510.repositories.chat.ChatMessage
import com.survivalcoding.a510.utils.DummyAIResponses
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application, private val roomId: Int) : AndroidViewModel(application) {
    private val messageDao = ChatDatabase.getDatabase(application).chatMessageDao()

    // StateFlow로 메시지 목록을 관리
    val allMessages: StateFlow<List<ChatMessage>> = messageDao.getMessagesByRoomId(roomId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 메시지 전송
    fun sendMessage(content: String) {
        viewModelScope.launch {
            messageDao.insertMessage(ChatMessage(roomId = roomId, content = content, isAiMessage = false))
            val aiResponse = generateAiResponse(content)
            messageDao.insertMessage(ChatMessage(roomId = roomId, content = aiResponse, isAiMessage = true))
        }
    }

    // AI 응답 생성 함수
    private fun generateAiResponse(userMessage: String): String = DummyAIResponses.getResponse(userMessage)

    // 채팅 기록 삭제
    fun clearChat() {
        viewModelScope.launch { messageDao.deleteMessagesByRoomId(roomId) }
    }
}
