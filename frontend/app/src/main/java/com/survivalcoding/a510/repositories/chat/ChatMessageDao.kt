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

    // 특정 채팅방의 마지막 메시지 삭제
    @Query("DELETE FROM messages WHERE roomId = :roomId AND id = (SELECT MAX(id) FROM messages WHERE roomId = :roomId)")
    suspend fun deleteLastMessage(roomId: Int)

    // 특정 채팅방의 가장 최근 메시지에 이미지 URL을 업데이트
    // 로컬저장소로 경로 변경할 때 쓰는 쿼리
    @Query("""
        UPDATE messages 
        SET imageUrl = :imageUrl 
        WHERE roomId = :roomId 
        AND id = (SELECT MAX(id) FROM messages WHERE roomId = :roomId)
    """)
    suspend fun updateLastMessageImageUrl(roomId: Int, imageUrl: String)

    // 특정 이미지 URL을 포함하는 모든 메시지를 조회
    @Query("SELECT * FROM messages WHERE imageUrl = :imageUrl")
    suspend fun getMessagesByImageUrl(imageUrl: String): List<ChatMessage>

    @Insert
    suspend fun insertMessageAndGetId(message: ChatMessage): Long  // Room에서는 Int 대신 Long을 사용

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: Long)

    // 동기식으로 메시지 가져오기 (로딩 메시지 찾기 위함)
    @Query("SELECT * FROM messages WHERE roomId = :roomId ORDER BY timestamp DESC")
    suspend fun getMessagesByRoomIdSync(roomId: Int): List<ChatMessage>

}