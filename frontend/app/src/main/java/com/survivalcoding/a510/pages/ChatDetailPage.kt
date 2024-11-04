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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.platform.LocalDensity
import android.util.Log

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFE3F2FD))
    ) {
        TopAppBar(
            modifier = Modifier
                .height(60.dp),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFE3F2FD)
            ),
            title = {
                Text(
                    text = chatData?.name ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.offset(y = (19).dp, x = 15.dp)
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(20.dp)
                        .offset(y = (21).dp, x = 8.dp)
                ) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                }
            },
            actions = {
                val onSearchClick = { /* 나중에 함수 넣기 */ }
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.offset(y = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "검색"
                    )
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
                                    !TimeUtils.formatChatTime(message.timestamp).equals(
                                        TimeUtils.formatChatTime(previousMessage.timestamp)
                                    )
                            )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = if (message.isAiMessage) 8.dp else 0.dp,
                                end = if (!message.isAiMessage) (0).dp else 8.dp
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
                            name = if (showProfile) chatData?.name else null
                        )
                    }

                    // 이 메시지가 해당 날짜의 마지막 메시지인지 확인
                    val isLastMessageOfDay = previousMessage != null &&
                            !TimeUtils.isSameDay(message.timestamp, previousMessage.timestamp)

                    // 날짜의 마지막 메시지라면 날짜 구분선 표시
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
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 4.dp
                                    )
                                )
                            }
                        }
                    }

                    // 마지막 메시지라면 첫 날짜 구분선 표시
                    if (index == messageList.size - 1) {
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
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 4.dp
                                    )
                                )
                            }
                        }
                    }
                }
            }


        TextInput(
            modifier = Modifier
                .imePadding(),
            value = messageText,
            onValueChange = { messageText = it },
            onSendClick = {
                if (messageText.isNotBlank()) {
                    viewModel.sendMessage(messageText)
                    messageText = ""
                }
            }
        )
    }
}
