package com.survivalcoding.a510.pages

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
import com.survivalcoding.a510.components.NextButton
import com.survivalcoding.a510.routers.Routes

data class Character(
    var name: String,
    var quote: String,
    var imageResource: Int,
)
val randomCharacters = arrayOf(
    Character("Fuddy", "만나서 반가워!", R.drawable.welcome_fuddy),
    Character("Tuddy", "만나서 반가워.", R.drawable.welcome_tuddy),
//    Character("Sunny", "만나서 반가워!", R.drawable.rabbit2),
//    Character("Luna", "만나서 반가워.", R.drawable.otter2),
)

// Get a random image from the array
val selectedCharacter = randomCharacters.random()

@Composable
fun WelcomePage(
    navController: NavHostController,
    isLoggedIn: Boolean,
    onKakaoLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(color = Color.White)
            .fillMaxSize(),
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