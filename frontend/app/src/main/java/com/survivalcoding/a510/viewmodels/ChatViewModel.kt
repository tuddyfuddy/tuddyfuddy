package com.survivalcoding.a510.viewmodels

import android.app.Application
import android.net.Uri
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.survivalcoding.a510.repositories.chat.ChatDatabase
import com.survivalcoding.a510.repositories.chat.ChatMessage
import com.survivalcoding.a510.repositories.chat.ChatRepository
import com.survivalcoding.a510.services.RetrofitClient
import com.survivalcoding.a510.services.chat.ChatRequest
import com.survivalcoding.a510.services.chat.ChatService
import com.survivalcoding.a510.services.chat.ImageService
import com.survivalcoding.a510.services.chat.getMessageList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class ChatViewModel(application: Application, private val roomId: Int) : AndroidViewModel(application) {
    private val database = ChatDatabase.getDatabase(application)
    private val messageDao = database.chatMessageDao()
    private val chatInfoDao = database.chatInfoDao()
    private val aiChatService = RetrofitClient.aiChatService
    private val repository: ChatRepository = ChatRepository(application)

    val allMessages: StateFlow<List<ChatMessage>> = messageDao.getMessagesByRoomId(roomId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _currentSearchIndex = MutableStateFlow(0)
    val currentSearchIndex = _currentSearchIndex.asStateFlow()

    private val _searchMatches = MutableStateFlow<List<Int>>(emptyList())
    val searchMatches = _searchMatches.asStateFlow()

    @OptIn(kotlinx.coroutines.FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val searchResults = searchQuery
        .debounce(300L)
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                flowOf(emptyList())
            } else {
                repository.searchMessages(roomId, query)
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun updateSearchQuery(query: String) {
        viewModelScope.launch {
            _searchQuery.value = query

            if (query.isBlank()) {
                _searchMatches.value = emptyList()
                _currentSearchIndex.value = 0
                return@launch
            }

            val messages = allMessages.value.reversed()
            val matches = messages.mapIndexedNotNull { index, message ->
                if (message.content.contains(query, ignoreCase = true)) index else null
            }
            _searchMatches.value = matches
            _currentSearchIndex.value = if (matches.isNotEmpty()) 0 else -1
        }
    }

    fun moveToNextMatch(listState: LazyListState, screenHeight: Int) {
        viewModelScope.launch {
            if (_currentSearchIndex.value < _searchMatches.value.size - 1) {
                _currentSearchIndex.value += 1
                scrollToCurrentMatch(listState, screenHeight)
            }
        }
    }

    fun moveToPreviousMatch(listState: LazyListState, screenHeight: Int) {
        viewModelScope.launch {
            if (_currentSearchIndex.value > 0) {
                _currentSearchIndex.value -= 1
                scrollToCurrentMatch(listState, screenHeight)
            }
        }
    }



    private suspend fun scrollToCurrentMatch(listState: LazyListState, screenHeight: Int) {
        val targetIndex = _searchMatches.value[_currentSearchIndex.value]
        // 화면어디에 검색한 단어가 있는 말풍선이 보이게 할지 정하는 숫자
        val targetOffset = (screenHeight * 0.8).toInt()

        listState.scrollToItem(
            index = targetIndex,
            scrollOffset = -targetOffset
        )
    }

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

            // 채팅방 정보 업데이트 (마지막 메시지 표시해주는거)
            chatInfoDao.updateLastMessage(
                chatId = roomId,
                message = content,
                timestamp = System.currentTimeMillis()
            )

            // 백그라운드 서비스 시작시키기
            ChatService.startService(getApplication(), roomId, content)
        }
    }

    // 채팅방 들어가면 읽지 않은 메세지 수 다시 0으로 초기화
    fun markAsRead() {
        viewModelScope.launch {
            chatInfoDao.updateUnreadCount(roomId, 0)
        }
    }

    // 이미지 업로드 처리
    fun handleImageUpload(uri: Uri) {
        viewModelScope.launch {
            try {
                // 사용자의 이미지 메시지 저장
                messageDao.insertMessage(
                    ChatMessage(
                        roomId = roomId,
                        content = "",  // 이미지인 경우 content는 비워둠
                        isAiMessage = false,
                        isImage = true,
                        imageUrl = uri.toString()  // 로컬 이미지 URI 저장
                    )
                )

                // 이미지 업로드 및 분석
                val response = ImageService.uploadAndAnalyzeImage(getApplication(), uri)

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!.result

                    // 채팅방 정보 업데이트 (채팅방 목록에서는 "이미지를 보냈습니다" 표시)
                    chatInfoDao.updateLastMessage(
                        chatId = roomId,
                        message = "이미지를 보냈습니다",
                        timestamp = System.currentTimeMillis()
                    )

                    // ChatService로 이미지 분석 결과 전달
                    ChatService.startService(
                        getApplication(),
                        roomId,
                        "사용자가 이미지를 공유했습니다.\n이미지 URL: ${result.imageUrl}\n이미지 설명: ${result.description}"
                    )

                } else {
                    val errorMessage = when (response.code()) {
                        413 -> "이미지 크기가 너무 큽니다. 더 작은 이미지를 선택해주세요."
                        415 -> "지원하지 않는 이미지 형식입니다. JPG, PNG 형식을 사용해주세요."
                        else -> "이미지 처리 중 오류가 발생했습니다. (Error: ${response.code()})"
                    }
                    messageDao.insertMessage(
                        ChatMessage(
                            roomId = roomId,
                            content = errorMessage,
                            isAiMessage = true
                        )
                    )
                }
            } catch (e: Exception) {
                // 구체적인 예외 처리
                val errorMessage = when (e) {
                    is OutOfMemoryError -> "이미지가 너무 큽니다. 더 작은 이미지를 선택해주세요."
                    is SecurityException -> "이미지 접근 권한이 없습니다. 권한을 확인해주세요."
                    is IllegalArgumentException -> "올바르지 않은 이미지 파일입니다. 다른 이미지를 선택해주세요."
                    else -> "이미지 처리 중 오류가 발생했습니다. 다시 시도해주세요.\n${e.message}"
                }

                messageDao.insertMessage(
                    ChatMessage(
                        roomId = roomId,
                        content = errorMessage,
                        isAiMessage = true
                    )
                )
            }
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
    }
}