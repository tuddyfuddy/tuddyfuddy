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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WelcomeViewModel : ViewModel() {
    fun sendHealthData(context: Context) {
        viewModelScope.launch {
            delay(6000) // 1분 대기

            try {
                val dummyHealthData = DummyHealthData.healthList.random()
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
                        responseHealthData.sleepMinutes < 300 -> "'오늘 잠 잘 못잤어? 안좋은 꿈 꿨어?' 라고 답장해줘, 다른 말은 절대로 하지말고 ''안에 있는 말만 해줘."
                        responseHealthData.heartRate > 100 -> "'무슨 일 있어? 좋은일이야 나쁜일이야?' 라고 답장해줘, 다른 말은 절대로 하지말고."
                        responseHealthData.heartRate < 60 -> "'무슨 일 있어?'라고 답장해줘, 다른 말은 절대로 하지말고."
                        responseHealthData.stressLevel > 50 -> "'기분 안 좋은 일 있어? 무슨 일 있으면 나한테 말해봐'라고 답장해줘, 다른 말은 절대로 하지말고."
                        responseHealthData.steps > 10000 -> "'오늘 엄청 많이 걸었네! 벌써 1만보 넘게 걸었어'라고 답장해줘, 다른 말은 절대로 하지말고."
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