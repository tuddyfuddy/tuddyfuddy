package com.survivalcoding.a510.pages

import android.app.Application
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
import com.survivalcoding.a510.routers.Routes
import com.survivalcoding.a510.utils.TimeUtils
import com.survivalcoding.a510.viewmodels.ChatListViewModel
import com.survivalcoding.a510.viewmodels.ChatListViewModelFactory
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.survivalcoding.a510.R
import com.survivalcoding.a510.components.AnimatedChatListBottom

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
                    Column {
                        Text(
                            text = "Tuddy Fuddy",
                            fontSize = 35.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row {
                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "나만을 위한 다양한 AI 친구들",
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.eland_choice))
                            )
                        }
                    }
//                        Text(
//                            text = "Tuddy Fuddy",
//                            fontSize = 38.sp
//                        )
//                        Spacer(modifier = Modifier.width(10.dp))
//
//                        Column {
//
//                            Spacer(modifier = Modifier.height(20.dp))
//
//                            Text(
//                                text = "나만을 위한 AI 친구",
//                                fontSize = 13.sp,
//                            )
//                        }
                }

                // 채팅방 목록
                Box(
                    modifier = Modifier
                        .weight(1f)  // 남은 공간 모두 차지
                        .fillMaxWidth()
                ) {
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

            }

            AnimatedChatListBottom(
                modifier = Modifier.align(Alignment.BottomEnd)
            )


        }
    }
}