package com.survivalcoding.a510.repositories.chat

import android.content.Context
import kotlinx.coroutines.flow.Flow

class ChatRepository(context: Context) { // 클래스 kind 사용
    private val chatMessageDao: ChatMessageDao = ChatDatabase.getDatabase(context).chatMessageDao()
    private val chatInfoDao: ChatInfoDao = ChatDatabase.getDatabase(context).chatInfoDao()

    fun getMessagesByRoomId(roomId: Int): Flow<List<ChatMessage>> =
        chatMessageDao.getMessagesByRoomId(roomId)

    fun searchMessages(roomId: Int, query: String): Flow<List<ChatMessage>> =
        chatMessageDao.searchMessages(roomId, query)

}