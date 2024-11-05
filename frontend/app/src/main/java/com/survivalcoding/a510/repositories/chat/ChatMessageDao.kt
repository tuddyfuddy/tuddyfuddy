package com.survivalcoding.a510.repositories.chat

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM messages WHERE roomId = :roomId ORDER BY timestamp ASC")
    fun getMessagesByRoomId(roomId: Int): Flow<List<ChatMessage>>

    @Insert
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM messages WHERE roomId = :roomId")
    suspend fun deleteMessagesByRoomId(roomId: Int)

    // 각 채팅방의 메세지 수를 세는 건데 ChatInfoDao에서 전체 채팅방으로 세서 해도 될거같아서 미사용중.
    // 추후 사용할거 같은 기능이라 남겨둠
    @Query("SELECT COUNT(*) FROM messages WHERE roomId = :roomId")
    suspend fun getMessageCount(roomId: Int): Int

    // 검색 관련 쿼리 추가
    @Query("""
        SELECT * FROM messages 
        WHERE roomId = :roomId 
        AND content LIKE '%' || :searchQuery || '%' 
        ORDER BY timestamp DESC
    """)
    fun searchMessages(roomId: Int, searchQuery: String): Flow<List<ChatMessage>>

    // 검색 결과 개수를 반환하는 쿼리
    @Query("""
        SELECT COUNT(*) FROM messages 
        WHERE roomId = :roomId 
        AND content LIKE '%' || :searchQuery || '%'
    """)
    suspend fun getSearchResultCount(roomId: Int, searchQuery: String): Int
}