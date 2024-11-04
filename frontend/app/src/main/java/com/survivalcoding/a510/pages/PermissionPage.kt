package com.survivalcoding.a510.pages

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.survivalcoding.a510.components.ChatListItem
import com.survivalcoding.a510.components.TopBar
import com.survivalcoding.a510.components.CircleCharacter
import com.survivalcoding.a510.routers.Routes
import com.survivalcoding.a510.utils.TimeUtils
import com.survivalcoding.a510.viewmodels.ChatListViewModel
import com.survivalcoding.a510.viewmodels.ChatListViewModelFactory

@Composable
fun PermissionPage (
                    navController: NavController,
                    viewModel: ChatListViewModel = viewModel(
                        factory = ChatListViewModelFactory(
                            LocalContext.current.applicationContext as Application
                        )
                    )
) {
    val chatList by viewModel.chatList.collectAsState()

    androidx.compose.material3.Scaffold(
        topBar = { TopBar() }
    ) { paddingValues ->
        Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 16.dp),
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
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth()
                    )
                }
            }
            Box(
                modifier = androidx.compose.ui.Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 25.dp, bottom = 20.dp)
            ) {
                Row {
                    Box(
                        modifier = androidx.compose.ui.Modifier.offset(y = (-35).dp, x = 5.dp)
                    )
                    Spacer(modifier = androidx.compose.ui.Modifier.width(14.dp))

                    CircleCharacter(onClick = {})
                }
            }
        }
    }
}

