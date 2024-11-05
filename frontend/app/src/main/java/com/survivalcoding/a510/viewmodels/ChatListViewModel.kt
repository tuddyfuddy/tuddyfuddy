package com.survivalcoding.a510.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.survivalcoding.a510.mocks.DummyAIData
import com.survivalcoding.a510.repositories.chat.ChatDatabase
import com.survivalcoding.a510.repositories.chat.ChatInfo
import com.survivalcoding.a510.repositories.chat.ChatMessage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatListViewModel(application: Application) : AndroidViewModel(application) {
    private val database = ChatDatabase.getDatabase(application)
    private val chatInfoDao = database.chatInfoDao()
    private val chatMessageDao = database.chatMessageDao()

    val chatList: StateFlow<List<ChatInfo>> = chatInfoDao.getAllChats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        initializeDefaultChats()
    }

    private fun initializeDefaultChats() {
        viewModelScope.launch {
            // DB가 비어있을 때만 초기 데이터 생성
            if (chatInfoDao.getChatCount() == 0) {
                DummyAIData.chatList.forEach { chatData ->
                    // ChatInfo 추가
                    chatInfoDao.insertChat(
                        ChatInfo(
                            id = chatData.id,
                            profileImage = chatData.profileImage,
                            name = chatData.name,
                            lastMessage = chatData.message,
                            unreadCount = chatData.unreadCount
                        )
                    )

                    // 초기 메시지 추가
                    chatMessageDao.insertMessage(
                        ChatMessage(
                            roomId = chatData.id,
                            content = chatData.message,
                            isAiMessage = true,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }
}
