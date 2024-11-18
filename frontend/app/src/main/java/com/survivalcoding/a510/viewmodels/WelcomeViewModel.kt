package com.survivalcoding.a510.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.survivalcoding.a510.mocks.DummyHealthData
import com.survivalcoding.a510.services.HealthRequest
import com.survivalcoding.a510.services.RetrofitClient
import com.survivalcoding.a510.services.chat.ChatService
import com.survivalcoding.a510.services.sendHealthDataWithLogging
import com.survivalcoding.a510.utils.DataIndexManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WelcomeViewModel : ViewModel() {

    private companion object {

        // 웰컴페이지 입장 후 헬스데이터 전송 및 응답 딜레이
        const val HEALTH_DATA_DELAY = 6000L

        // 헬스데이터 답변 선택 기준 변수
        const val SLEEP_TIME = 300
        const val HEART_UP_RATE = 100
        const val HEART_DOWN_RATE = 60
        const val STRESS_LEVEL = 50
        const val STEP_RATE = 10000

        // 헬스데이터에 따른 AI 답변 케이스
        const val SLEEP_TALK = "'오늘 잠 잘 못잤어? 안좋은 꿈 꿨어?' 라고 답장해줘, 다른 말은 절대로 하지말고 ''안에 있는 말만 해줘."
        const val HEART_UP_TALK = "'무슨 일 있어?'라고 답장해줘, 다른 말은 절대로 하지말고."
        const val HEART_DOWN_TALK = "'무슨 일 있어?'라고 답장해줘, 다른 말은 절대로 하지말고."
        const val STRESS_TALK = "'기분 안 좋은 일 있어? 무슨 일 있으면 나한테 말해봐'라고 답장해줘, 다른 말은 절대로 하지말고."
        const val STEP_TALK = "'오늘 엄청 많이 걸었네! 벌써 1만보 넘게 걸었어'라고 답장해줘, 다른 말은 절대로 하지말고."
    }

    fun sendHealthData(context: Context) {
        viewModelScope.launch {
            delay(HEALTH_DATA_DELAY) // 1분 대기

            try {
                val nextIndex = DataIndexManager.getNextIndex(DummyHealthData.healthList.size)
                val dummyHealthData = DummyHealthData.healthList[nextIndex]

                val response = RetrofitClient.healthService.sendHealthDataWithLogging(
                    HealthRequest(
                        heartRate = dummyHealthData.heartRate,
                        steps = dummyHealthData.steps,
                        sleepMinutes = dummyHealthData.sleepMinutes,
                        stressLevel = dummyHealthData.stressLevel
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val responseHealthData = response.body()!!.result

                    val message = when {
                        responseHealthData.sleepMinutes < SLEEP_TIME -> SLEEP_TALK
                        responseHealthData.heartRate > HEART_UP_RATE -> HEART_UP_TALK
                        responseHealthData.heartRate < HEART_DOWN_RATE -> HEART_DOWN_TALK
                        responseHealthData.stressLevel > STRESS_LEVEL -> STRESS_TALK
                        responseHealthData.steps > STEP_RATE -> STEP_TALK
                        else -> null
                    }

                    message?.let {
                        ChatService.startService(
                            context = context,
                            roomId = 2,
                            content = it,
                            loadingMessageId = null
                        )
                    }
                }
                Log.d("웰컴페이지 헬스", "헬스 API 응답: ${response.body()}")
            } catch (e: Exception) {
                Log.e("웰컴페이지 헬스", "헬스 API 에러", e)
            }
        }
    }
}