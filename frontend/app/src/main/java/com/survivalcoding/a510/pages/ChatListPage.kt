package com.survivalcoding.a510.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.survivalcoding.a510.R
import com.survivalcoding.a510.Routes
import com.survivalcoding.a510.components.ChatListItem
import com.survivalcoding.a510.components.TopBar
import com.survivalcoding.a510.components.CircleCharacter
import com.survivalcoding.a510.components.SpeechBubble

data class ChatData(
    val id: Int,
    val profileImage: Int,
    val name: String,
    val message: String,
    val timestamp: String,
    val unreadCount: Int = 0
)

@Composable
fun ChatListPage(navController: NavController) {
    val chatList = listOf(
        ChatData(
            id = 1,
            profileImage = R.drawable.cha,
            name = "활명수",
            message = "늦었다고 생각할 때가 진짜 늦은 거야",
            timestamp = "2분 전",
            unreadCount = 2
        ),
        ChatData(
            id = 2,
            profileImage = R.drawable.back,
            name = "백지헌",
            message = "갑자기 비내리는거 같은데 ㅠㅠ",
            timestamp = "오후 2:40",
            unreadCount = 3
        ),
    )

    Scaffold(
        topBar = { TopBar() }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 16.dp),
            ) {
                items(chatList) { chat ->
                    ChatListItem(
                        profileImage = chat.profileImage,
                        name = chat.name,
                        message = chat.message,
                        timestamp = chat.timestamp,
                        unreadCount = chat.unreadCount,
                        onClick = {
                            navController.navigate(Routes.chatDetail(chat.id))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 25.dp, bottom = 20.dp)
            ) {
                Row {
                    Box(
                        modifier = Modifier.offset(y = (-35).dp, x = 5.dp)
                    ) {
                        SpeechBubble(text = "더 많은 권한을 허용하면 \n 더 나은 대답을 줄 수 있어요.")
                    }
                    Spacer(modifier = Modifier.width(14.dp))

                    CircleCharacter(onClick = {})
                }
            }
        }
    }
}