package com.survivalcoding.a510

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
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.survivalcoding.a510.components.KakaoLoginButton
import com.survivalcoding.a510.states.AuthState
import com.survivalcoding.a510.viewmodels.MainViewModel
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

object Routes {
    const val CONTENT_SCREEN = "contentScreen"
    const val CHAT_LIST = "chatListPage"
    const val CHAT_DETAIL = "chatDetailPage/{chatId}"

    fun chatDetail(chatId: Int) = "chatDetailPage/$chatId"
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(applicationContext) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            A510Theme {
                val navController = rememberNavController()
                val authState by viewModel.authState.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.CONTENT_SCREEN
                    ) {
                        composable(Routes.CONTENT_SCREEN) {
                            ContentScreen(
                                modifier = Modifier.padding(innerPadding),
                                navController = navController,
                                authState = authState,
                                onKakaoLoginClick = {
                                    viewModel.handleKakaoLogin(
                                        onSuccess = {
                                            navController.navigate(Routes.CHAT_LIST)
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
    authState: AuthState,
    onKakaoLoginClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        when (authState) {
            is AuthState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp)
                )
            }
            is AuthState.Success -> {
                LaunchedEffect(Unit) {
                    navController.navigate(Routes.CHAT_LIST)
                }
            }
            is AuthState.Error -> {
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
            }
            else -> {
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
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (authState !is AuthState.Success) {
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