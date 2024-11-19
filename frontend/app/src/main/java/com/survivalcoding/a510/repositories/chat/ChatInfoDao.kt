package com.survivalcoding.a510.repositories.chat

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatInfoDao {
    @Query("SELECT * FROM chat_info ORDER BY lastMessageTime DESC")
    fun getAllChats(): Flow<List<ChatInfo>>

    @Query("SELECT * FROM chat_info WHERE id = :chatId")
    suspend fun getChatById(chatId: Int): ChatInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chatInfo: ChatInfo)

    @Query("UPDATE chat_info SET lastMessage = :message, lastMessageTime = :timestamp WHERE id = :chatId")
    suspend fun updateLastMessage(chatId: Int, message: String, timestamp: Long)

    @Query("UPDATE chat_info SET unreadCount = :count WHERE id = :chatId")
    suspend fun updateUnreadCount(chatId: Int, count: Int)

    @Query("SELECT COUNT(*) FROM chat_info")
    suspend fun getChatCount(): Int


}