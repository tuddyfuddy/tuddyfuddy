package com.survivalcoding.a510.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.survivalcoding.a510.repositories.chat.ChatDatabase
import com.survivalcoding.a510.repositories.chat.ChatMessage
import com.survivalcoding.a510.services.RetrofitClient
import com.survivalcoding.a510.services.chat.ChatRequest
import com.survivalcoding.a510.services.chat.getMessageList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application, private val roomId: Int) : AndroidViewModel(application) {
    private val database = ChatDatabase.getDatabase(application)
    private val messageDao = database.chatMessageDao()
    private val chatInfoDao = database.chatInfoDao()
    private val aiChatService = RetrofitClient.aiChatService

    val allMessages: StateFlow<List<ChatMessage>> = messageDao.getMessagesByRoomId(roomId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun sendMessage(content: String) {
        viewModelScope.launch {
            // 사용자 메시지 저장
            messageDao.insertMessage(
                ChatMessage(
                    roomId = roomId,
                    content = content,
                    isAiMessage = false
                )
            )

            // 채팅방 정보 업데이트 (마지막 메시지)
            chatInfoDao.updateLastMessage(
                chatId = roomId,
                message = content,
                timestamp = System.currentTimeMillis()
            )

            try {
                // API 호출 - 새로운 요청 형식 사용
                val response = aiChatService.sendChatMessage(
                    type = roomId,
                    request = ChatRequest(text = content)  // 수정된 부분
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

                    // AI의 마지막 메시지로 채팅방 정보 업데이트
                    response.body()?.getMessageList()?.lastOrNull()?.let { lastAiMessage ->
                        chatInfoDao.updateLastMessage(
                            chatId = roomId,
                            message = lastAiMessage,
                            timestamp = System.currentTimeMillis()
                        )

                        // 읽지 않은 메시지 수 증가
                        val currentChat = chatInfoDao.getChatById(roomId)
                        currentChat?.let {
                            chatInfoDao.updateUnreadCount(
                                chatId = roomId,
                                count = it.unreadCount + 1
                            )
                        }
                    }
                    // else 부분 수정
                } else {
                    // 에러 메시지
                    val errorMessage = "죄송해요, 메시지를 받지 못했어요."

                    // 에러 메시지 저장
                    messageDao.insertMessage(
                        ChatMessage(
                            roomId = roomId,
                            content = errorMessage,
                            isAiMessage = true
                        )
                    )

                    // 채팅방 정보도 에러 메시지로 업데이트
                    chatInfoDao.updateLastMessage(
                        chatId = roomId,
                        message = errorMessage,
                        timestamp = System.currentTimeMillis()
                    )

                    // 읽지 않은 메시지 수 증가
                    val currentChat = chatInfoDao.getChatById(roomId)
                    currentChat?.let {
                        chatInfoDao.updateUnreadCount(
                            chatId = roomId,
                            count = it.unreadCount + 1
                        )
                    }
                }
            } catch (e: Exception) {
                val errorMessage = "네트워크 오류가 발생했어요. 인터넷 연결을 확인해주세요."

                // 네트워크 에러 메시지 저장
                messageDao.insertMessage(
                    ChatMessage(
                        roomId = roomId,
                        content = errorMessage,
                        isAiMessage = true
                    )
                )

                // 채팅방 정보도 에러 메시지로 업데이트
                chatInfoDao.updateLastMessage(
                    chatId = roomId,
                    message = errorMessage,
                    timestamp = System.currentTimeMillis()
                )

                // 읽지 않은 메시지 수 증가
                val currentChat = chatInfoDao.getChatById(roomId)
                currentChat?.let {
                    chatInfoDao.updateUnreadCount(
                        chatId = roomId,
                        count = it.unreadCount + 1
                    )
                }
            }
        }
    }

    // 채팅방 들어가면 읽지 않은 메세지 수 다시 0으로 초기화
    fun markAsRead() {
        viewModelScope.launch {
            chatInfoDao.updateUnreadCount(roomId, 0)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            // 선택한 채팅방의 메시지만 전체 삭제
            messageDao.deleteMessagesByRoomId(roomId)

            chatInfoDao.updateLastMessage(
                chatId = roomId,
                message = "친구와 새로운 대화를 시작해보세요!",
                timestamp = System.currentTimeMillis()
            )
        }
    }}