package com.survivalcoding.a510.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider
import com.survivalcoding.a510.repositories.chat.ChatDatabase
import com.survivalcoding.a510.repositories.chat.ChatMessage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val database = ChatDatabase.getDatabase(application)
    private val messageDao = database.chatMessageDao()

    // StateFlow로 메시지 목록을 관리하는거
    val allMessages: StateFlow<List<ChatMessage>> = messageDao.getAllMessages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 내가 입력한 메세지 보내기
    fun sendMessage(content: String) {
        viewModelScope.launch {
            // 보낸 메세지 저장
            messageDao.insertMessage(
                ChatMessage(
                    content = content,
                    isAiMessage = false
                )
            )

            // Ai 답변 생성하고 저장하는 로직
            val aiResponse = generateAiResponse(content)
            messageDao.insertMessage(
                ChatMessage(
                    content = aiResponse,
                    isAiMessage = true
                )
            )
        }
    }

    // Ai가 주는 답변 일단 하드코딩 해논거
    private fun generateAiResponse(userMessage: String): String {
        return "안녕하세요! '$userMessage'라고 하셨네요."
    }

    // 채팅 기록 전부 삭제 (근데 아직 안쓰임, 안쓸거같긴한데)
    fun clearChat() {
        viewModelScope.launch {
            messageDao.deleteAllMessages()
        }
    }
}

class ChatViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}