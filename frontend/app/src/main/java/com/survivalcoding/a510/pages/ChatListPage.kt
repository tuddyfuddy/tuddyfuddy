package com.survivalcoding.a510.pages

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlin.system.exitProcess
import androidx.compose.runtime.mutableStateOf

@Composable
fun ChatListPage(
    navController: NavController,
    viewModel: ChatListViewModel = viewModel(
        factory = ChatListViewModelFactory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val context = LocalContext.current
    var doubleBackToExitPressedOnce = remember { mutableStateOf(false) }

    // 핸드폰 기본 뒤로가기 버튼 눌렀을 때 웰컴 페이지 대신에 어플이 꺼지게 하기
    BackHandler {
        if (doubleBackToExitPressedOnce.value) {
            exitProcess(0)
        } else {
            doubleBackToExitPressedOnce.value = true
            Toast.makeText(context, "한 번 더 누르면 어플이 종료됩니다", Toast.LENGTH_SHORT).show()

            Handler(Looper.getMainLooper()).postDelayed({
                doubleBackToExitPressedOnce.value = false
            }, 2000) // 2초 안에 2번 눌러야 꺼지게
        }
    }

    val chatList by viewModel.chatList.collectAsState()

    Scaffold(
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
                    Row {
//                        Text(
//                            text = "Tuddy Fuddy",
//                            fontSize = 35.sp
//                        )

                        Image(
                            painter = painterResource(id = R.drawable.logo_white),
                            contentDescription = "Tuddy Fuddy Logo",
                            modifier = Modifier.height(72.dp),
                            contentScale = ContentScale.Fit
                        )

                        Row {
                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "나만을 위한",
                                    fontSize = 16.sp,
                                    fontFamily = FontFamily(Font(R.font.eland_choice))
                                )
                                Text(
                                    text = "다양한 AI 친구들",
                                    fontSize = 20.sp,
                                    fontFamily = FontFamily(Font(R.font.eland_choice))
                                )
                            }
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