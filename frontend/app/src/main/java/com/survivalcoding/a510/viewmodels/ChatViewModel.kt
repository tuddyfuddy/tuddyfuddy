package com.survivalcoding.a510.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider
import com.survivalcoding.a510.repositories.chat.ChatDatabase
import com.survivalcoding.a510.repositories.chat.ChatMessage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val database = ChatDatabase.getDatabase(application)
    private val messageDao = database.chatMessageDao()

    // StateFlow로 메시지 목록을 관리하는거
    val allMessages: StateFlow<List<ChatMessage>> = messageDao.getAllMessages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 내가 입력한 메세지 보내기
    fun sendMessage(content: String) {
        viewModelScope.launch {
            // 보낸 메세지 저장
            messageDao.insertMessage(
                ChatMessage(
                    content = content,
                    isAiMessage = false
                )
            )

            // Ai 답변 생성하고 저장하는 로직
            val aiResponse = generateAiResponse(content)
            messageDao.insertMessage(
                ChatMessage(
                    content = aiResponse,
                    isAiMessage = true
                )
            )
        }
    }

    // Ai가 주는 답변 일단 하드코딩 해논거
    private fun generateAiResponse(userMessage: String): String = when (userMessage) {
        "안녕" -> "안뇽!안뇽!"
        "하이" -> "하이~~!"
        "밥 먹었어?" -> "아니 아직 ㅠㅠ"
        "저녁 뭐 먹어?" -> "글쎄~? 아직 고민중이야! 너는?"
        "저녁 추천해줘" -> "너가 제일 좋아하는 음식이 뭔데?"
        "점심 추천해줘" -> "너가 제일 좋아하는 음식은 뭔데?"
        "아침 추천해줘" -> "아침은 간단하게 샐러드 어때?"
        "돈까스" -> "돈까스 좋지! 돈까쓰 이즈 마이 소울 푸드"
        "짜장면" -> "짜장면은 싫어 너무 많이 먹어서 물려"
        "국밥" -> "국밥 좋지 킹성비잖아! 요새는 아니려나?"
        "라면" -> "왜 밥 안 먹고 라면 먹어ㅠㅠ 내가 밥 사줄게 나와!"
        "소고기" -> "와 오늘 무슨 날이야? 복권이라도 당첨됐어?!"
        "삼겹살" -> "삼겹살에는 소주지! 나랑 같이 삼쏘할래?"
        "초밥" -> "초밥은 너무 비싸"
        "미워" -> "미워하지마 ㅠㅠ 내가 더 잘할게"
        "사랑해" -> "나두 사랑해 >3<"
        "고마워" -> "별말씀을~~"
        "잘래" -> "많이 피곤한가보네 얼른 자 잘자!"
        "ㅂㅂ" -> "ㅃㅃ"
        "바이바이" -> "잘 가~~ 나중에 또 연락해"
        "이희주" -> "세상에서 제일 지적인 우리 A510의 팀장님"
        "김인엽" -> "모르는거 다해주는 만능 인간 GPT"
        "강유미" -> "A510의 홍일점! SSAFY의 카리나!"
        "제갈덕" -> "나를 만들어주신 아버지....오..마이 파더"
        "지경근" -> "저번에 상도 받았다던 프론트마스터!"
        "최영빈" -> "A510의 귀여운 마"
        "그럴까?" -> "그래! 그러자구~~"
        "싫어" -> "그래?ㅠㅠ 아쉽네"
        "좋아" -> "나도 좋아!!"
        "SSAFY" -> "얼른 탈출해"
        "취업하고싶어" -> "하지말고 나랑 놀자"
        "커피 마시고 싶다" -> "그만 마셔"
        "없어" -> "그럼 어쩔 수 없지"
        "덥다" -> "그니까 날씨 왜 이래 쪄죽겠어"
        "춥다" -> "패딩 입어! 잔뜩 껴입어!"
        "김민수" -> "운동 잘하고 착하고 똑똑한 형 언제 안아줄거야"
        "이상무" -> "안기면 듬직해서 참 좋아"
        "이한솔" -> "제일 착한 누나 시험 화이팅"
        "이소희" -> "영빈이 좀 그만 추방해!"
        "이찬규" -> "서울대입구 자취방 기필코 놀러가보리"
        "김지윤" -> "잘못말한거 아냐? 김지헌! SSAFY의 백지헌이라던데?"
        "집에 가고 싶어" -> "어림도 없지!"
        "이번 프로젝트 잘되려나" -> "자율? 이번에 주제 뭐라고 했지?"
        "Ai 친구" -> "아 맞다맞다 그거였지. 음,,,,"
        "왜 별로야?" -> "아니? 무조건 장관상감이지 이건!"
        "진짜로?" -> "내가 장담한다. 이건 최소 입상감이야"
        "ㅋㅋㅋㅋㅋㅋ고맙다" -> "너희가 잘만든건데 뭘ㅋㅋㅋㅋㅋㅋ"
        "ㅋㅋㅋ" -> "ㅋㅋㅋㅋㅋㅋㅋ"
        else -> "미안해 아직 학습중이라서 '$userMessage'가 뭔지 모르겠어 ㅠㅠ 금방 똑똑해져서 돌아올게!"
    }

    // 채팅 기록 전부 삭제 (근데 아직 안쓰임, 안쓸거같긴한데)
    fun clearChat() {
        viewModelScope.launch {
            messageDao.deleteAllMessages()
        }
    }
}

class ChatViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}