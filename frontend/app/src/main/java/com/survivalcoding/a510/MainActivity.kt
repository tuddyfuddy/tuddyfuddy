package com.survivalcoding.a510

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.survivalcoding.a510.ui.theme.A510Theme
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import com.survivalcoding.a510.routers.AppRouter
import com.survivalcoding.a510.states.AuthState
import com.survivalcoding.a510.viewmodels.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        viewModel.initializeTokenRepository(this)

        setContent {
            A510Theme {
                val navController = rememberNavController()
                val authState by viewModel.authState.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val isLoggedIn = authState is AuthState.Success
                    AppRouter(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        isLoggedIn = isLoggedIn,
                        onKakaoLoginClick = {
                            viewModel.handleKakaoLogin(
                                activity = this@MainActivity,
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
            }
        }
    }
}
