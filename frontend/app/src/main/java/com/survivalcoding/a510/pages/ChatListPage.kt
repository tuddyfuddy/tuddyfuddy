package com.survivalcoding.a510.pages

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.survivalcoding.a510.components.ChatListItem
import com.survivalcoding.a510.components.TopBar
import com.survivalcoding.a510.components.CircleCharacter
import com.survivalcoding.a510.components.SpeechBubble
import com.survivalcoding.a510.routers.Routes
import com.survivalcoding.a510.utils.TimeUtils
import com.survivalcoding.a510.viewmodels.ChatListViewModel
import com.survivalcoding.a510.viewmodels.ChatListViewModelFactory
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.survivalcoding.a510.R

@Composable
fun ChatListPage(
    navController: NavController,
    viewModel: ChatListViewModel = viewModel(
        factory = ChatListViewModelFactory(
            LocalContext.current.applicationContext as Application
        )
    )
) {

    val chatList by viewModel.chatList.collectAsState()

    Scaffold(
        // topBar = { TopBar() }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 25.dp, start = 16.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
//                    Column(
//                        modifier = Modifier.weight(1f)
//                    ) {
//                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Tuddy Fuddy",
                            fontSize = 38.sp
//                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                        Column {

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "나만을 위한 AI 친구",
                                fontSize = 13.sp,
//                                style = MaterialTheme.typography.bodySmall,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

//                    }
//                    Image(
//                        painter = painterResource(id = R.drawable.maincha),
//                        contentDescription = "Main Character",
//                        modifier = Modifier
//                            .size(80.dp)
//                    )
                }

                // 채팅방 목록
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                ) {
                    items(chatList) { chat ->
                        ChatListItem(
                            profileImage = chat.profileImage,
                            name = chat.name,
                            message = chat.lastMessage,
                            timestamp = TimeUtils.formatChatTime(chat.lastMessageTime),
                            unreadCount = chat.unreadCount,
                            onClick = {
                                navController.navigate(Routes.chatDetail(chat.id))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 하단 캐럭터 및 말풍선
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 25.dp, bottom = 20.dp)
            ) {
                Row {
                    Box(
                        modifier = Modifier.offset(y = (-35).dp, x = 5.dp)
                    ) {
                        SpeechBubble(text = "조금은 차갑지만 마음은 따뜻한 Tuddy \n언제나 따뜻하게 나를 반겨주는 Fuddy")
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    CircleCharacter(onClick = {})
                }
            }
        }
    }
}