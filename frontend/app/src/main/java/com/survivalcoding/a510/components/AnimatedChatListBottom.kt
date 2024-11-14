package com.survivalcoding.a510.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.survivalcoding.a510.R
import kotlinx.coroutines.delay

data class CharacterInfo(
    val name: String,
    val description: String,
    val imageRes: Int
)

@Composable
fun AnimatedChatListBottom(
    modifier: Modifier = Modifier
) {
    val characters = listOf(
        CharacterInfo(
            "Fuddy",
            "언제나 따뜻하게 날 반겨주는\n공감능력 100% 나만의 친구",
            R.drawable.big_girl2
        ),
        CharacterInfo(
            "Tuddy",
            "말투는 조금 차갑고 무뚝뚝해도\n속마음은 따뜻한 나만의 친구",
            R.drawable.big_boy2
        ),
        CharacterInfo(
            "달님이",
            "서늘하고 어두운 밤하늘을 밝혀 주는\n달과 같은 나만의 해달 친구",
            R.drawable.otter2
        ),
        CharacterInfo(
            "햇님이",
            "햇살처럼 날 따뜻하게 만들어주는\n복실복실한 나만의 토끼 친구",
            R.drawable.rabbit2
        ),
    )

    var currentIndex by remember { mutableStateOf(0) }
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = true) {
        // 페이드인아웃 애니메이션 효과 추가하기
        while (true) {  // 무한루프 뱅뺑이 돌아라
            delay(4000) // 3초마다 컾모넌트 내용 바꾸기
            isVisible = false
            delay(500) // 애니메이션으로 컴포넌트 없어지는 시간
            currentIndex = (currentIndex + 1) % characters.size
            isVisible = true
            delay(500) // 애니메이션으로 컴포넌트 나타나는 시간
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(500)
    )

    Box(
        modifier = modifier
    ) {
        Row {
            Box(
                modifier = Modifier.offset(y = 100.dp, x = 20.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier.alpha(alpha)
                    ) {
                        Text(
                            text = characters[currentIndex].name,
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Box(
                        modifier = Modifier
                            .offset(x = 3.dp)
                            .alpha(alpha)
                    ) {
                        Text(
                            text = characters[currentIndex].description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .offset(y = 53.dp, x = (10).dp)
                    .alpha(alpha)
            ) {
                Image(
                    painter = painterResource(id = characters[currentIndex].imageRes),
                    contentDescription = "${characters[currentIndex].name} Character Image",
                    modifier = Modifier.size(300.dp)
                )
            }
        }
    }
}