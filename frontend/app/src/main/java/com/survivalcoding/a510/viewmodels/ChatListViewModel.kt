package com.survivalcoding.a510.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.survivalcoding.a510.mocks.DummyAIData
import com.survivalcoding.a510.repositories.chat.ChatDatabase
import com.survivalcoding.a510.repositories.chat.ChatInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class ChatListViewModel(application: Application) : AndroidViewModel(application) {
    private val chatInfoDao = ChatDatabase.getDatabase(application).chatInfoDao()

    val chatList: StateFlow<List<ChatInfo>> = chatInfoDao.getAllChats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    init {
        // 앱 최초 실행시 초기 데이터 삽입
        initializeDefaultChats()
    }

    private fun initializeDefaultChats() {
        viewModelScope.launch {
            // 현재 채팅 목록이 비어있을 때만 초기 데이터 삽입
            if (chatList.value.isEmpty()) {
                // DummyAIData의 데이터를 ChatInfo로 변환하여 삽입
                DummyAIData.chatList.forEach { chatData ->
                    chatInfoDao.insertChat(
                        ChatInfo(
                            id = chatData.id,
                            profileImage = chatData.profileImage,
                            name = chatData.name,
                            lastMessage = chatData.message,
                            unreadCount = chatData.unreadCount
                        )
                    )
                }
            }
        }
    }
}