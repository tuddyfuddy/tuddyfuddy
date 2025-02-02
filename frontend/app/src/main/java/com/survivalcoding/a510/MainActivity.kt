package com.survivalcoding.a510

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.survivalcoding.a510.ui.theme.A510Theme
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.survivalcoding.a510.data.TokenManager
import com.survivalcoding.a510.routers.AppRouter
import com.survivalcoding.a510.routers.Routes
import com.survivalcoding.a510.services.RetrofitClient
import com.survivalcoding.a510.states.AuthState
import com.survivalcoding.a510.viewmodels.MainViewModel

// 인증상태를 관리하고 네비게이션 처리
class MainActivity : ComponentActivity() {
    // ViewModel 인스턴스
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FCM 토큰 초기화
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                TokenManager(this).saveFCMToken(token)
            }
        }

        // 시스템 윈도우 설정
        WindowCompat.setDecorFitsSystemWindows(window, true)

        // RetrofitClient와 ViewModel 초기화
        RetrofitClient.initialize(this)
        viewModel.initialize(this)

        setContent {
            A510Theme {
                // 네비게이션 컨트롤러 생성
                val navController = rememberNavController()
                // 인증 상태 관찰
                val authState by viewModel.authState.collectAsState()

                // 알림으로부터의 시작 처리
                LaunchedEffect(Unit) {
                    if (intent?.getBooleanExtra("fromNotification", false) == true) {
                        val chatRoomId = intent.getIntExtra("chatRoomId", -1)
                        if (chatRoomId != -1) {
                            if (authState is AuthState.Success) {
                                // 먼저 채팅 리스트로 이동하고, 그 다음 채팅방으로 이동
                                navController.navigate(Routes.CHAT_LIST) {
                                    // 시작 화면까지 모두 제거
                                    popUpTo(Routes.WELCOME_PAGE) { inclusive = true }
                                }
                                // 채팅방으로 이동
                                navController.navigate(Routes.chatDetail(chatRoomId))
                            }
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // 로그인 상태 확인
                    val isLoggedIn = authState is AuthState.Success

                    AppRouter(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        isLoggedIn = isLoggedIn,
                        onKakaoLoginClick = {
                            // 카카오 로그인 처리
                            viewModel.handleKakaoLogin(
                                activity = this@MainActivity,
                                onSuccess = {
                                    navController.navigate(Routes.TERMS_AGREEMENT) {
                                        popUpTo(Routes.WELCOME_PAGE) { inclusive = true }
                                    }
                                },
                                onError = { error ->
                                    // 로그인 실패 시 에러 메시지 표시
                                    Toast.makeText(
                                        this@MainActivity,
                                        "로그인 실패: ${error.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}