package com.survivalcoding.a510.mocks

import com.survivalcoding.a510.R
import com.survivalcoding.a510.models.ChatData

object DummyAIData {
    private val chatList = listOf(
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
        )

    )

    fun getChatById(id: Int): ChatData? = chatList.find { it.id == id }
}