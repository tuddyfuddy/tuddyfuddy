package com.survivalcoding.a510.viewmodels

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateMapOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatViewModel : ViewModel() {
    // 채팅방별 메시지 맵을 StateFlow로 관리
    private val _messages = mutableStateMapOf<Int, List<String>>()
    val messages: Map<Int, List<String>> = _messages

    // 각 채팅방의 메시지 목록 가져오기
    fun getMessages(chatId: Int): List<String> {
        if (!_messages.containsKey(chatId)) {
            _messages[chatId] = emptyList()  // 새로운 채팅방 초기화
        }
        return _messages[chatId] ?: emptyList()
    }

    // 메시지 추가
    fun addMessage(chatId: Int, message: String) {
        val currentMessages = getMessages(chatId)
        _messages[chatId] = currentMessages + message
    }
}