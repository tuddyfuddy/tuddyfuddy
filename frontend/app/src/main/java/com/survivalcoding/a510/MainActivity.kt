package com.survivalcoding.a510

import androidx.compose.ui.tooling.preview.Preview
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import com.survivalcoding.a510.ui.theme.A510Theme
import com.survivalcoding.a510.components.NextButton
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.survivalcoding.a510.pages.ChatDetailPage
import com.survivalcoding.a510.pages.ChatListPage
import android.widget.Toast
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.survivalcoding.a510.components.KakaoLoginButton
import com.survivalcoding.a510.viewmodels.MainViewModel

object Routes {
    const val CONTENT_SCREEN = "contentScreen"
    const val CHAT_LIST = "chatListPage"
    const val CHAT_DETAIL = "chatDetailPage/{chatId}"

    fun chatDetail(chatId: Int) = "chatDetailPage/$chatId"
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            A510Theme {
                val navController = rememberNavController()
                val isLoggedIn by viewModel.isLoggedIn.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.CONTENT_SCREEN
                    ) {
                        composable(Routes.CONTENT_SCREEN) {
                            ContentScreen(
                                modifier = Modifier.padding(innerPadding),
                                navController = navController,
                                isLoggedIn = isLoggedIn,
                                onKakaoLoginClick = {
                                    viewModel.handleKakaoLogin(
                                        context = this@MainActivity,
                                        onSuccess = {
                                            navController.navigate("chatListPage")
                                        },
                                        onError = { error ->
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
                        composable(Routes.CHAT_LIST) {
                            ChatListPage(navController)
                        }
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
            }
        }
    }
}

@Composable
fun ContentScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    isLoggedIn: Boolean,
    onKakaoLoginClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Greeting(
            name = "Android",
            modifier = Modifier
                .padding(top = 350.dp)
                .padding(start = 120.dp)
        )

        if (isLoggedIn) {
            NextButton(
                onClick = { navController.navigate("chatListPage") },
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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!!!!!",
        modifier = modifier.padding(16.dp)
    )
}


