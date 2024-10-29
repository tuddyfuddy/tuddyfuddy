package com.survivalcoding.a510.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.survivalcoding.a510.R
import com.survivalcoding.a510.components.ChatListItem
import com.survivalcoding.a510.components.TopBar

// 채팅 메시지 데이터 클래스
data class ChatData(
    val id: Int,
    val profileImage: Int,
    val name: String,
    val message: String,
    val timestamp: String
)

@Composable
fun ChatListPage(navController: NavController) {
    // 임시 데이터 (나중에 백엔드에서 가져올 데이터)
    val chatList = listOf(
        ChatData(
            id = 1,
            profileImage = R.drawable.cha,
            name = "활명수",
            message = "늦었다고 생각할 때가 진짜 늦은 거야",
            timestamp = "2분 전"
        ),
        ChatData(
            id = 2,
            profileImage = R.drawable.back,
            name = "백지현",
            message = "갑자기 비내리는거 같은데 ㅠㅠ",
            timestamp = "오후 2:40"
        ),
        // 필요한 만큼 데이터 추가
    )

    Scaffold(
        topBar = { TopBar() }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatList) { chat ->
                ChatListItem(
                    profileImage = chat.profileImage,
                    name = chat.name,
                    message = chat.message,
                    timestamp = chat.timestamp,
                    onClick = {
                        // 채팅방으로 이동하는 네비게이션 추가
                        // navController.navigate("chatRoom/${chat.id}")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}