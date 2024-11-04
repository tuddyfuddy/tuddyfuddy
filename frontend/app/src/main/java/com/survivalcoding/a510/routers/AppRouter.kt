package com.survivalcoding.a510.routers

import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.survivalcoding.a510.components.NextButton
import com.survivalcoding.a510.components.KakaoLoginButton
import com.survivalcoding.a510.pages.ChatDetailPage
import com.survivalcoding.a510.pages.ChatListPage
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.survivalcoding.a510.R


object Routes {
    const val WELCOME_SCREEN = "welcomeScreen"
    const val CHAT_LIST = "chatListPage"
    const val CHAT_DETAIL = "chatDetailPage/{chatId}"

    fun chatDetail(chatId: Int) = "chatDetailPage/$chatId"
}

@Composable
fun AppRouter(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean,
    onKakaoLoginClick: () -> Unit
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME_SCREEN,
        modifier = modifier
    ) {
        // 환영 화면 라우트
        composable(Routes.WELCOME_SCREEN) {
            WelcomeScreen(
                navController = navController,
                isLoggedIn = isLoggedIn,
                onKakaoLoginClick = {
                    onKakaoLoginClick.invoke()
                    navController.navigate(Routes.CHAT_LIST)
                }
            )
        }

        // 채팅 리스트 화면 라우트
        composable(Routes.CHAT_LIST) {
            ChatListPage(navController)
        }

        // 채팅 상세 화면 라우트
        composable(
            route = Routes.CHAT_DETAIL,
            arguments = listOf(
                navArgument("chatId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getInt("chatId") ?: return@composable
            ChatDetailPage(navController, chatId)
        }
    }
}

@Composable
fun WelcomeScreen(
    navController: NavHostController,
    isLoggedIn: Boolean,
    onKakaoLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Image(
            painter = painterResource(id = R.drawable.circlecha),
            contentDescription = "Circle Character",
            modifier = Modifier.size(350.dp)
        )
        Text(
            text = "만나서 반가워!",
            modifier = Modifier.padding(24.dp),
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        if (isLoggedIn) {
            NextButton(
                onClick = { navController.navigate(Routes.CHAT_LIST) },
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            KakaoLoginButton(
                onClick = onKakaoLoginClick,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}
