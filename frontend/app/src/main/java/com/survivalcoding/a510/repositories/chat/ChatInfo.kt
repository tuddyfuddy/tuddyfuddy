package com.survivalcoding.a510.repositories.chat

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_info")
data class ChatInfo(
    @PrimaryKey
    val id: Int,
    val profileImage: Int,
    val name: String,
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0
)