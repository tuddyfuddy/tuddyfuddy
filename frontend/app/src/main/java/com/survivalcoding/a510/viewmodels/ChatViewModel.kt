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
import com.survivalcoding.a510.services.chat.ImageProcessingService
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
    private val _pendingMessages = MutableStateFlow<List<String>>(emptyList())
    private var loadingMessageId: Long? = null

    init {
        viewModelScope.launch {
            @OptIn(kotlinx.coroutines.FlowPreview::class)
            _pendingMessages  // 묶음 전송 처리를 위한 보류중인 메세지
                .debounce(4000)
                .collect { messages ->
                    if (messages.isNotEmpty()) {
                        val combinedContent = messages.joinToString("<br>")
                        ChatService.startService(
                            getApplication(),
                            roomId,
                            combinedContent,
                            loadingMessageId
                        )
                        _pendingMessages.value = emptyList()
                    }
                }
        }
    }

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

            // 이전 로딩 메시지가 있다면 삭제
            loadingMessageId?.let { id ->
                messageDao.deleteMessageById(id)
            }

            // 새로운 로딩 메시지 추가
            val loadingMessage = ChatMessage(
                roomId = roomId,
                content = "",
                isAiMessage = true,
                isLoading = true
            )

            val insertedId = messageDao.insertMessageAndGetId(loadingMessage)
            loadingMessageId = insertedId

            // 채팅방 정보 업데이트 (마지막 메시지 표시해주는거)
            chatInfoDao.updateLastMessage(
                chatId = roomId,
                message = content,
                timestamp = System.currentTimeMillis()
            )

            // pending 메시지 목록에 추가
            _pendingMessages.value += content
        }
    }

    fun handleImageUpload(uri: Uri) {
        viewModelScope.launch {
            try {
                // 사용자가 보낸 이미지 메시지 저장
                messageDao.insertMessage(
                    ChatMessage(
                        roomId = roomId,
                        content = "",
                        isAiMessage = false,
                        isImage = true,
                        imageUrl = uri.toString()
                    )
                )

                // 채팅방 정보 업데이트
                chatInfoDao.updateLastMessage(
                    chatId = roomId,
                    message = "사진을 전송했습니다.",
                    timestamp = System.currentTimeMillis()
                )

                // 이미지 처리를 ChatService로 위임
                ImageProcessingService.startService(getApplication(), roomId, uri)
            } catch (e: Exception) {
                val errorMessage = "이미지 처리 중 오류가 발생했습니다. 다시 시도해주세요.\n${e.message}"
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

    override fun onCleared() {
        super.onCleared()
        // ViewModel 제거될 때 사용자가 보냈던 묶음 메시지가 있다면 무조건 전송
        if (_pendingMessages.value.isNotEmpty()) {
            val combinedContent = _pendingMessages.value.joinToString("<br>")
            ChatService.startService(
                getApplication(),
                roomId,
                combinedContent,
                loadingMessageId
            )
        }
    }

    // 채팅방 들어가면 읽지 않은 메세지 수 다시 0으로 초기화
    fun markAsRead() {
        viewModelScope.launch {
            chatInfoDao.updateUnreadCount(roomId, 0)
        }
    }
}