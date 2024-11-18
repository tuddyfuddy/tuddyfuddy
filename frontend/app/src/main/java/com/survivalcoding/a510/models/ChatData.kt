package com.survivalcoding.a510.models

data class ChatData(
    val id: Int,
    val profileImage: Int,
    val name: String,
    val message: String,
    val timestamp: String,
    val unreadCount: Int
)