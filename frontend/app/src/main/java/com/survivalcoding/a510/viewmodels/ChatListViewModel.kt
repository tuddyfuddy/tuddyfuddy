package com.survivalcoding.a510.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.survivalcoding.a510.R
import com.survivalcoding.a510.models.ChatData
import com.survivalcoding.a510.repositories.chat.ChatDatabase
import com.survivalcoding.a510.repositories.chat.ChatMessage
import com.survivalcoding.a510.utils.TimeUtils
import kotlinx.coroutines.flow.*

class ChatListViewModel(application: Application) : AndroidViewModel(application) {
    private val messageDao = ChatDatabase.getDatabase(application).chatMessageDao()
    private val chatInfoMap = mapOf(1 to Triple(1, R.drawable.cha, "활명수"), 2 to Triple(2, R.drawable.back, "백지헌"))

    // 채팅방 목록 실시간 가져오기
    val chatList: StateFlow<List<ChatData>> = messageDao.getMessagesByRoomId(1)
        .combine(messageDao.getMessagesByRoomId(2)) { messages1, messages2 ->
            createChatList(messages1, messages2)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun createChatList(messages1: List<ChatMessage>, messages2: List<ChatMessage>): List<ChatData> {
        return listOf(messages1 to 1, messages2 to 2).mapNotNull { (messages, roomId) ->
            val (id, profileImage, name) = chatInfoMap[roomId] ?: return@mapNotNull null
            val lastMessage = messages.lastOrNull()

            ChatData(
                id = id,
                profileImage = profileImage,
                name = name,
                message = lastMessage?.content.orEmpty(),
                timestamp = lastMessage?.let { TimeUtils.formatChatListTime(it.timestamp) }.orEmpty(),
                unreadCount = 0
            )
        }
    }
}
