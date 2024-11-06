package com.survivalcoding.a510.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.platform.LocalContext
import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.survivalcoding.a510.components.ChatBubble
import com.survivalcoding.a510.components.TextInput
import com.survivalcoding.a510.viewmodels.ChatViewModel
import com.survivalcoding.a510.viewmodels.ChatViewModelFactory
import com.survivalcoding.a510.utils.TimeUtils
import com.survivalcoding.a510.mocks.DummyAIData
import androidx.compose.foundation.layout.imePadding
import com.survivalcoding.a510.services.chat.ChatService
import androidx.compose.animation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.coroutines.launch
import com.survivalcoding.a510.components.UpDownButton
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.os.Build


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailPage(
    navController: NavController,
    chatId: Int,
    viewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(
            LocalContext.current.applicationContext as Application,
            chatId
        )
    )
) {
    var messageText by remember { mutableStateOf("") }
    val messages by viewModel.allMessages.collectAsState()
    val chatData = remember { DummyAIData.getChatById(chatId) }
    var showMenu by remember { mutableStateOf(false) }
    var isSearchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val currentSearchIndex by viewModel.currentSearchIndex.collectAsState()
    val searchMatches by viewModel.searchMatches.collectAsState()
    val context = LocalContext.current

    // 이미지 선택 launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.handleImageUpload(uri)
        }
    }

    // 권한 요청 launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        }
    }

    // 검색어가 변경될 때마다 첫 번째 검색 결과로 스크롤
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            val messageList = messages.reversed()
            val firstMatchIndex = messageList.indexOfFirst {
                it.content.contains(searchQuery, ignoreCase = true)
            }

            if (firstMatchIndex != -1) {
                // 스크린 높이의 30% 위치에 아이템이 오도록
                val targetOffset = (screenHeight * 0.8).toInt()

                coroutineScope.launch {
                    listState.scrollToItem(
                        index = firstMatchIndex,
                        scrollOffset = -targetOffset
                    )
                }
            }
        }
    }

    // 뒤로가기 버튼 처리
    BackHandler {
        if (isSearchMode) {
            isSearchMode = false
            searchQuery = ""
            viewModel.updateSearchQuery("")
        } else {
            navController.popBackStack()
        }
    }

    DisposableEffect(Unit) {
        ChatService.setActiveChatRoom(chatId)
        viewModel.markAsRead()

        onDispose {
            ChatService.setActiveChatRoom(null)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFE3F2FD))
    ) {
        TopAppBar(
            modifier = Modifier.height(60.dp),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFE3F2FD)
            ),
            title = {
                if (isSearchMode) {
                    TextField(
                        modifier = Modifier
                            .padding(top = 3.dp, start = 20.dp)
                            .height(60.dp)
                            .fillMaxWidth(),
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.updateSearchQuery(it)
                        },
                        placeholder = { Text("대화 내용 검색", fontSize = 14.sp ) },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 14.sp  // 입력 텍스트 크기 줄임
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE3F2FD),
                            unfocusedContainerColor = Color(0xFFE3F2FD),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        keyboardActions = KeyboardActions(
                            onSearch = {
                            }
                        )
                    )
                } else {
                    Text(
                        text = chatData?.name ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.offset(y = 19.dp, x = 15.dp)
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        if (isSearchMode) {
                            isSearchMode = false
                            searchQuery = ""
                            viewModel.updateSearchQuery("")
                        } else {
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .size(20.dp)
                        .offset(y = 21.dp, x = 8.dp)
                ) {
                    Icon(
                        imageVector = if (isSearchMode) Icons.Default.Close else Icons.Default.ArrowBackIosNew,
                        contentDescription = if (isSearchMode) "Close search" else "Back"
                    )
                }
            },
            actions = {
                if (!isSearchMode) {
                    IconButton(
                        onClick = { isSearchMode = true },
                        modifier = Modifier.offset(y = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "검색"
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.offset(y = 6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "메뉴"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                viewModel.clearChat()
                                showMenu = false
                            },
                            text = { Text("채팅기록 삭제") }
                        )
                    }
                }
            }
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            val messageList = messages.reversed()


            items(messageList.size) { index ->
                val message = messageList[index]
                val previousMessage = if (index < messageList.size - 1) messageList[index + 1] else null

                val showProfile = message.isAiMessage && (
                        previousMessage == null ||
                                !previousMessage.isAiMessage ||
                                !TimeUtils.formatChatTime(message.timestamp)
                                    .equals(TimeUtils.formatChatTime(previousMessage.timestamp))
                        )

                val isFirstInSequence = previousMessage == null || previousMessage.isAiMessage != message.isAiMessage

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = if (message.isAiMessage) 8.dp else 0.dp,
                            end = if (!message.isAiMessage) 0.dp else 8.dp
                        )
                        .offset(x = if (!message.isAiMessage) 10.dp else 0.dp),
                    horizontalArrangement = if (message.isAiMessage)
                        Arrangement.Start else Arrangement.End
                ) {
                    ChatBubble(
                        text = message.content,
                        timestamp = message.timestamp,
                        isAiMessage = message.isAiMessage,
                        profileImage = if (showProfile) chatData?.profileImage else null,
                        name = if (showProfile) chatData?.name else null,
                        isFirstInSequence = isFirstInSequence,
                        searchQuery = searchQuery,
                        isImage = message.isImage,
                        imageUrl = message.imageUrl
                    )
                }

                val isLastMessageOfDay = previousMessage != null &&
                        !TimeUtils.isSameDay(message.timestamp, previousMessage.timestamp)

                if (isLastMessageOfDay) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0x33000000),
                            modifier = Modifier.defaultMinSize(minHeight = 22.dp)
                        ) {
                            Text(
                                text = TimeUtils.formatDate(message.timestamp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
        if (isSearchMode) {
            UpDownButton(
                onUpClick = {
                    viewModel.moveToPreviousMatch(listState, screenHeight)
                },
                onDownClick = {
                    viewModel.moveToNextMatch(listState, screenHeight)
                },
                currentIndex = currentSearchIndex,
                totalResults = searchMatches.size,
                modifier = Modifier.imePadding()
            )
        } else {
            TextInput(
                modifier = Modifier.imePadding(),
                value = messageText,
                onValueChange = { messageText = it },
                onSendClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    }
                },
                onCameraClick = {
                    when {
                        // Android 14 이상 (API 34)
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                            imagePickerLauncher.launch("image/*")
                        }
                        // Android 13 (API 33)
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                            when {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.READ_MEDIA_IMAGES
                                ) == PackageManager.PERMISSION_GRANTED -> {
                                    imagePickerLauncher.launch("image/*")
                                }
                                else -> {
                                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                                }
                            }
                        }
                        // Android 10-12 (API 29-32)
                        else -> {
                            when {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                ) == PackageManager.PERMISSION_GRANTED -> {
                                    imagePickerLauncher.launch("image/*")
                                }
                                else -> {
                                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}