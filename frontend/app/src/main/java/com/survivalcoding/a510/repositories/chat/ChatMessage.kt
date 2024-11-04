package com.survivalcoding.a510.repositories.chat

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val roomId: Int,
    val content: String,
    val isAiMessage: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)