package com.survivalcoding.a510.routers

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.survivalcoding.a510.pages.ChatDetailPage
import com.survivalcoding.a510.pages.ChatListPage
import androidx.compose.runtime.LaunchedEffect
import com.survivalcoding.a510.pages.PermissionPage
import com.survivalcoding.a510.pages.SettingPage
import com.survivalcoding.a510.pages.SignUpCompletePage
import com.survivalcoding.a510.pages.TermsAgreementPage
import com.survivalcoding.a510.pages.WelcomePage
import kotlinx.coroutines.delay


object Routes {
    const val WELCOME_PAGE = "welcomeScreen"
    const val TERMS_AGREEMENT = "termsAgreement"
    const val PERMISSION_PAGE = "permissionPage"
    const val SIGNUP_COMPLETE = "signupComplete"
    const val CHAT_LIST = "chatListPage"
    const val CHAT_DETAIL = "chatDetailPage/{chatId}"
    const val SETTING_PAGE = "settingPage"

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
        startDestination = Routes.WELCOME_PAGE,
        modifier = modifier
    ) {
        // 환영 화면 라우트
        composable(Routes.WELCOME_PAGE) {
            WelcomePage(
                navController = navController,
                isLoggedIn = isLoggedIn,
                onKakaoLoginClick = {
                    onKakaoLoginClick.invoke()
                    if (isLoggedIn) {
                        navController.navigate(Routes.CHAT_LIST) {
                            popUpTo(Routes.WELCOME_PAGE) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.TERMS_AGREEMENT) {
                            popUpTo(Routes.WELCOME_PAGE) { inclusive = true }
                        }
                    }
                }
            )
        }

        // 약관 동의 페이지
        composable(Routes.TERMS_AGREEMENT) {
            TermsAgreementPage(
                onNextClick = {
                    navController.navigate(Routes.PERMISSION_PAGE)
                }
            )
        }

        // 권한 설정 페이지
        composable(Routes.PERMISSION_PAGE) {
            PermissionPage(
                onNextClick = {
                    navController.navigate(Routes.SIGNUP_COMPLETE)
                }
            )
        }

        // 가입 완료 페이지
        composable(Routes.SIGNUP_COMPLETE) {
            SignUpCompletePage()
            LaunchedEffect(Unit) {
                delay(2000)
                navController.navigate(Routes.CHAT_LIST) {
                    popUpTo(Routes.WELCOME_PAGE) { inclusive = true }
                }
            }
        }

        // 권한 설정 페이지
        composable(Routes.SETTING_PAGE) {
            SettingPage(navController)
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

