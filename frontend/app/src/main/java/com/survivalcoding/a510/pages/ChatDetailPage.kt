package com.survivalcoding.a510.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.survivalcoding.a510.R
import com.survivalcoding.a510.components.ChatTopBar

@Composable
fun ChatDetailPage(
    navController: NavController,
    chatId: Int
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val chatData = remember {
        when (chatId) {
            1 -> ChatData(
                id = 1,
                profileImage = R.drawable.cha,
                name = "활명수",
                message = "늦었다고 생각할 때가 진짜 늦은 거야",
                timestamp = "2분 전",
                unreadCount = 2
            )
            2 -> ChatData(
                id = 2,
                profileImage = R.drawable.back,
                name = "백지헌",
                message = "갑자기 비내리는거 같은데 ㅠㅠ",
                timestamp = "오후 2:40",
                unreadCount = 3
            )
            else -> null
        }
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                modifier = Modifier,
                title = chatData?.name ?: "",
                onBackClick = { navController.popBackStack() },
                onSearchClick = { /* 검색 기능 */ },
                onMenuClick = { /* 메뉴 기능 */ }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(color = Color.White)
        ) {
        }
    }
}