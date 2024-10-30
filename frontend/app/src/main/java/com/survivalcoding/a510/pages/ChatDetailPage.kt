package com.survivalcoding.a510.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.survivalcoding.a510.components.ChatTopBar

@Composable
fun ChatDetailPage(
    navController: NavController,
    chatId: Int
) {
    Scaffold(
        topBar = {
            ChatTopBar(
                modifier = Modifier
                    .padding(top = 60.dp),
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
        )
    }
}