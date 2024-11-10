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
    // Room 데이터베이스 인스턴스를 초기화
    private val database = ChatDatabase.getDatabase(application)

    // 채팅 메시지 데이터 접근용 DAO(Data Access Object)
    private val messageDao = database.chatMessageDao()

    // 채팅방 정보 데이터 접근용 DAO
    private val chatInfoDao = database.chatInfoDao()

    // AI 채팅 서비스와 통신하기 위한 Retrofit 서비스 인스턴스 (근데 지금 안쓰임)
    private val aiChatService = RetrofitClient.aiChatService

    // 채팅 관련 데이터 처리를 담당하는 Repository
    private val repository: ChatRepository = ChatRepository(application)

    // 아직 서버로 전송되지 않은 대기 중인 메시지들을 저장
    // debounce를 통해 여러 메시지를 모아서 한 번에 전송하기 위해 사용
    private val _pendingMessages = MutableStateFlow<List<String>>(emptyList())

    // 현재 로딩 중인 메시지의 ID를 저장
    // AI 응답을 기다리는 동안 표시되는 로딩 메시지를 관리 하기 위해 사용
    private var loadingMessageId: Long? = null

    // 이미지 분석 결과를 임시 저장하기 위한 데이터 클래스
    private data class PendingImageAnalysis(
        val index: Int,     // 텍스트, 이미지 순서 유지하기 위한 인덱스
        val result: String  // 이미지 분석 결과 데스크립션
    )

    // 처리 대기 중인 이미지 분석 결과들 저장하는 Flow (이제 안쓰이는거임)
    private val _pendingImageResults = MutableStateFlow<List<PendingImageAnalysis>>(emptyList())

    // 전체 메시지(텍스트+이미지) 순서 관리용
    private data class PendingItem(
        val index: Int,
        val content: String,
        val isImage: Boolean = false
    )

    // 메시지의 순서를 정하기 위한거 (메세지 추가되면 1씩 증가)
    private var messageIndex = 0

    // 메세지 순서를 지키기 위해 해당 메세지들을 저장하는 FLow
    private val _orderedPendingItems = MutableStateFlow<List<PendingItem>>(emptyList())

    // ViewModel이 생성될 때 무조건 제일 먼저 실행되야 하는 것들은 init에 포함시키기
    init {
        viewModelScope.launch {
            launch {
                // 메세지 모아서 처리하는 로직
                @OptIn(kotlinx.coroutines.FlowPreview::class)
                _pendingMessages
                    .debounce(6000)
                    .collect { messages ->
                        if (messages.isNotEmpty()) {

                            // 마지막 메세지 기준 정해진 N초 동안 대기 후 추가 입력 없으면 해당 메세지들 <br>로 구분해서 묶음
                            val combinedContent = messages.joinToString("<br>")

                            // 묶은 메세지들을 ChatService로 API 한 번에 모아서 보내기
                            ChatService.startService(
                                getApplication(),
                                roomId,
                                combinedContent,
                                loadingMessageId
                            )
                            // 그리고 API로 묶어서 요청 보냈으면 다시 메세지 초기화
                            _pendingMessages.value = emptyList()
                        }
                    }
            }

            // 이미지 처리 결과 받아오기
            launch {
                ImageProcessingService.getPendingMessageFlow().collect { pair ->
                    pair?.let { (messageRoomId, message) ->
                        // 현재 사용자 화면 채팅방이랑 같은 roomId인지 확인하기
                        if (messageRoomId == roomId) {

                            // 이전 로딩 아이콘이 있면 삭제
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

                            // 새로운 로딩 아이콘 만들기
                            val insertedId = messageDao.insertMessageAndGetId(loadingMessage)
                            loadingMessageId = insertedId

                            // 이미지 분석 결과가 오면 보류중인 메세지에 분석결과 추가하기
                            _pendingMessages.value += message
                            ImageProcessingService.clearPendingMessage()
                        }
                    }
                }
            }
        }
    }

    // 현재 채팅방 모든 메세지 관리하는 stateFlow
    val allMessages: StateFlow<List<ChatMessage>> =
        messageDao.getMessagesByRoomId(roomId) // DB에서 해당 채팅방의 모든 메세지 가져오기
            .stateIn( // Flow를 State Flow로 변경
                scope = viewModelScope,  // ViewModel과 생명주기 맞추기
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // 키워드 검색 기능 쿼리
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // 현재 검색한 키워드 검색 결과 찾기 (해당 키워드가 있는 화면으로 이동할 때 사용되는 인덱스)
    private val _currentSearchIndex = MutableStateFlow(0)
    val currentSearchIndex = _currentSearchIndex.asStateFlow()

    // 검색된 키워드들 위치랑 개수 관리 (해당 키워드가 몇개 검색됐는지, 화면 이동할 때 사용)
    private val _searchMatches = MutableStateFlow<List<Int>>(emptyList())
    val searchMatches = _searchMatches.asStateFlow()

    @OptIn(kotlinx.coroutines.FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val searchResults = searchQuery
        // 사용자가 검색할 때 검색 키워드 인식하는 속도 (0.3초마다 해당 키워드로 검색)
        .debounce(300L)

        // 새로운 검색어 인식되면 기존 검색 취소하고 새로운 검색만 실행하기
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

    // 검색한 키워드 처리하고 업데이트 하기
    fun updateSearchQuery(query: String) {
        viewModelScope.launch {
            _searchQuery.value = query

            if (query.isBlank()) {
                _searchMatches.value = emptyList()
                _currentSearchIndex.value = 0
                return@launch
            }

            // 메세지 목록을 역순으로 가져오기 (최신 메세지부터 보여주기 위해서)
            val messages = allMessages.value.reversed()

            // 각 메세지에서 검색 키워드 포함하고 있는지 확인
            // ignoreCase : 영어일때는 대문자 소문자 구분 없이 확인하기
            val matches = messages.mapIndexedNotNull { index, message ->
                if (message.content.contains(query, ignoreCase = true)) index else null
            }
            _searchMatches.value = matches
            _currentSearchIndex.value = if (matches.isNotEmpty()) 0 else -1
        }
    }

    // 다음 검색 키워드 화면으로 이동하기
    fun moveToNextMatch(listState: LazyListState, screenHeight: Int) {
        viewModelScope.launch {
            if (_currentSearchIndex.value < _searchMatches.value.size - 1) {
                _currentSearchIndex.value += 1
                scrollToCurrentMatch(listState, screenHeight)
            }
        }
    }

    // 이전 검색 키워드 화면으로 이동하기
    fun moveToPreviousMatch(listState: LazyListState, screenHeight: Int) {
        viewModelScope.launch {
            if (_currentSearchIndex.value > 0) {
                _currentSearchIndex.value -= 1
                scrollToCurrentMatch(listState, screenHeight)
            }
        }
    }

    // 검색 키워드 포함한 말풍선 위치 조정하는 함수
    private suspend fun scrollToCurrentMatch(listState: LazyListState, screenHeight: Int) {
        // 검색한 키워드가 포함된 메세지의 위치
        val targetIndex = _searchMatches.value[_currentSearchIndex.value]
        // 화면에서 검색 키워드가 포함된 메세지 말풍선이 어디에 위치할지 조정하는 변수
        val targetOffset = (screenHeight * 0.8).toInt()

        listState.scrollToItem(
            index = targetIndex,
            scrollOffset = -targetOffset
        )
    }

    // 사용자가 텍스트를 보내면 실행되는 함수
    fun sendMessage(content: String) {
        viewModelScope.launch {

            // 사용자 메시지 RoomDB에 저장
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

            // 로딩 메세지 ID 추가 (새로운 사용자 메세지가 추가되면 기존 로딩메세지 삭제하기 위해서)
            val insertedId = messageDao.insertMessageAndGetId(loadingMessage)
            loadingMessageId = insertedId

            // 채팅목록 페이지 업데이트
            // 채팅목록 페이지에서 마지막 메시지, 시간 표시해주는거
            chatInfoDao.updateLastMessage(
                chatId = roomId,
                message = content,
                timestamp = System.currentTimeMillis()
            )

            // 메세지 순서 저장
            val currentIndex = messageIndex++
            _orderedPendingItems.value += PendingItem(
                index = currentIndex,
                content = content,
                isImage = false
            )

            // pending 메시지 목록에 추가
            _pendingMessages.value += content
        }
    }

    // 사용자가 이미지를 보낸 경우 실행되는 함수
    fun handleImageUpload(uri: Uri) {
        viewModelScope.launch {
            try {
                // ImageProcessingService로 정보 전달
                ImageProcessingService.startService(getApplication(), roomId, uri)

                // 채팅목록 페이지 업데이트
                chatInfoDao.updateLastMessage(
                    chatId = roomId,
                    message = "사진을 전송했습니다.",
                    timestamp = System.currentTimeMillis()
                )
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

    // 해당 채팅방의 모든 채팅내역 삭제하는 함
    fun clearChat() {
        viewModelScope.launch {
            // 선택한 채팅방의 메시지만 전체 삭제
            messageDao.deleteMessagesByRoomId(roomId)

            // 채팅 내역 삭제 후 해당 채팅방 목록 페이지 ChatInfo 수정
            chatInfoDao.updateLastMessage(
                chatId = roomId,
                message = "친구와 새로운 대화를 시작해보세요!",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    // 채팅방 들어가면 읽지 않은 메세지 수 다시 0으로 초기화
    fun markAsRead() {
        viewModelScope.launch {
            chatInfoDao.updateUnreadCount(roomId, 0)
        }
    }



    // 백그라운드에서도 작업이 가능하도록 하는 함
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
}