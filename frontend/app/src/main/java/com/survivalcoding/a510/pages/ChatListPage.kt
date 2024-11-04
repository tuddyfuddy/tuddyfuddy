package com.survivalcoding.a510.pages

import android.app.Activity
import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.survivalcoding.a510.components.ChatListItem
import com.survivalcoding.a510.components.TopBar
import com.survivalcoding.a510.components.CircleCharacter
import com.survivalcoding.a510.components.SpeechBubble
import com.survivalcoding.a510.routers.Routes
import com.survivalcoding.a510.utils.TransparentSystemBars
import com.survivalcoding.a510.viewmodels.ChatListViewModel
import com.survivalcoding.a510.viewmodels.ChatListViewModelFactory

@Composable
fun ChatListPage(
    navController: NavController,
    viewModel: ChatListViewModel = viewModel(
        factory = ChatListViewModelFactory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
//    TransparentSystemBars(darkIcons = true)

    val chatList by viewModel.chatList.collectAsState()

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