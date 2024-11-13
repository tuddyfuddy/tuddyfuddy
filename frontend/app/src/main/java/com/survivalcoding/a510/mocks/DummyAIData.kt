package com.survivalcoding.a510.mocks

import com.survivalcoding.a510.R
import com.survivalcoding.a510.models.ChatData

object DummyAIData {
    val chatList = listOf(
        ChatData(
            id = 3,
            profileImage = R.drawable.otter,
            name = "달님이",
            message = "만나서 반가워, 너는 이름이 뭐야?",
            timestamp = "방금 전",
            unreadCount = 1
        ),
        ChatData(
            id = 4,
            profileImage = R.drawable.rabbit,
            name = "햇님이",
            message = "방가방가!! 나는 햇님이야! 너는 이름이 뭐야~?",
            timestamp = "방금 전",
            unreadCount = 1
        ),
        ChatData(
            id = 5,
            profileImage = R.drawable.group_profile,
            name = "햇님이, 달님이",
            message = "단톡방에서 여러 친구들과 함께 대화를 시작해보세요!",
            timestamp = "방금 전",
            unreadCount = 1
        ),
        ChatData(
            id = 2,
            profileImage = R.drawable.fuddy,
            name = "Fuddy",
            message = "하이하이!! 너는 이름이 뭐야?",
            timestamp = "방금 전",
            unreadCount = 1
        ),
        ChatData(
            id = 1,
            profileImage = R.drawable.tuddy,
            name = "Tuddy",
            message = "야 뭐하고 있냐",
            timestamp = "방금 전",
            unreadCount = 1
        ),
//        ChatData(
//            id = 6,
//            profileImage = R.drawable.tuddy,
//            name = "Test1111",
//            message = "Test1111",
//            timestamp = "방금 전",
//            unreadCount = 1
//        ),
//        ChatData(
//            id = 7,
//            profileImage = R.drawable.tuddy,
//            name = "Test2222",
//            message = "Test2222",
//            timestamp = "방금 전",
//            unreadCount = 1
//        ),
    )

    fun getChatById(id: Int): ChatData? = chatList.find { it.id == id }
}