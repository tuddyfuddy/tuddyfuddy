package com.survivalcoding.a510.mocks

import com.survivalcoding.a510.R
import com.survivalcoding.a510.models.ChatData

object DummyAIData {
    val chatList = listOf(
        ChatData(
            id = 1,
            profileImage = R.drawable.cha,
            name = "차은우",
            message = "야 대답해",
            timestamp = "방금 전",
            unreadCount = 1
        ),
        ChatData(
            id = 2,
            profileImage = R.drawable.back,
            name = "백지헌",
            message = "하이하이!! 너는 이름이 뭐야?",
            timestamp = "방금 전",
            unreadCount = 1
        ),
        ChatData(
            id = 3,
            profileImage = R.drawable.kim,
            name = "김유정",
            message = "심심하면 나랑 수다나 떨자",
            timestamp = "방금 전",
            unreadCount = 1
        ),
        ChatData(
            id = 4,
            profileImage = R.drawable.karina,
            name = "카리나",
            message = "이번 주말에 뭐해?",
            timestamp = "방금 전",
            unreadCount = 1
        ),
        ChatData(
            id = 5,
            profileImage = R.drawable.grouptalk_white,
            name = "김유정, 카리나",
            message = "단톡방에서 여러 친구들과 함께 대화를 시작해보세요!",
            timestamp = "방금 전",
            unreadCount = 1
        ),
    )

    fun getChatById(id: Int): ChatData? = chatList.find { it.id == id }
}