package com.survivalcoding.a510.mocks

import com.survivalcoding.a510.R
import com.survivalcoding.a510.models.ChatData

object DummyAIData {
    val chatList = listOf(
        ChatData(
            id = 1,
            profileImage = R.drawable.cha,
            name = "활명수",
            message = "반가워! 너는 이름이 뭐야?",
            timestamp = "방금 전",
            unreadCount = 1
        ),
        ChatData(
            id = 2,
            profileImage = R.drawable.back,
            name = "백지헌",
            message = "하이하이!! 얼른 나랑 놀아줘!!!!",
            timestamp = "방금 전",
            unreadCount = 1
        )

    )

    fun getChatById(id: Int): ChatData? = chatList.find { it.id == id }
}