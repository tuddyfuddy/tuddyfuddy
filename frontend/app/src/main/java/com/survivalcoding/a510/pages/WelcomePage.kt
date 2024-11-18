package com.survivalcoding.a510.pages

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.survivalcoding.a510.R
import com.survivalcoding.a510.components.KakaoLoginButton
import com.survivalcoding.a510.routers.Routes
import androidx.compose.foundation.clickable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import com.survivalcoding.a510.viewmodels.WelcomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewmodel.compose.viewModel

data class Character(
    var name: String,
    var quote: String,
    var imageResource: Int,
)
val randomCharacters = arrayOf(
    Character("Fuddy", "만나서 반가워!", R.drawable.welcome_fuddy),
    Character("Tuddy", "만나서 반가워.", R.drawable.welcome_tuddy),
)

val selectedCharacter = randomCharacters.random()

@Composable
fun WelcomePage(
    navController: NavHostController,
    isLoggedIn: Boolean,
    onKakaoLoginClick: () -> Unit,
    viewModel: WelcomeViewModel = viewModel()
) {
    val context = LocalContext.current

    // 웰컴페이지 왔는데 로그인 되어있을때만 헬스 API 보내기
    LaunchedEffect(Unit) {
        if (isLoggedIn) {
            viewModel.sendHealthData(context)
        }
    }

    // 아무 화면이나 터치안해도 자동으로 3초있다가 챗리스트 페이지로 가도록 설정
    if (isLoggedIn) {
        LaunchedEffect(Unit) {
            delay(3000)
            withContext(Dispatchers.Main) {
                navController.navigate(Routes.CHAT_LIST)
            }
        }
    }
    Column(
        modifier = Modifier
            .background(color = Color.White)
            .fillMaxSize()
            .then(
                if (isLoggedIn) {
                    Modifier.clickable { navController.navigate(Routes.CHAT_LIST) }
                } else {
                    Modifier
                }
            ),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Image(
            painter = painterResource(id = selectedCharacter.imageResource),
            contentDescription = "Circle Character",
            modifier = Modifier.size(350.dp)
        )
        Text(
            text = selectedCharacter.quote,
            modifier = Modifier.padding(24.dp),
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        if (!isLoggedIn) {
            KakaoLoginButton(
                onClick = onKakaoLoginClick,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}