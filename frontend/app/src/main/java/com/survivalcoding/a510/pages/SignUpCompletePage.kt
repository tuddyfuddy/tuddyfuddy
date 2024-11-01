package com.survivalcoding.a510.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.survivalcoding.a510.components.CheckIcon
import com.survivalcoding.a510.ui.theme.A510Theme
import kotlinx.coroutines.delay

@Composable
fun SignUpCompletePage() {
    var showText by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        delay(1200)
        showText = true
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 체크 아이콘을 담는 Box
        Box(
            modifier = Modifier.offset(y = (-32).dp),
            contentAlignment = Alignment.Center
        ) {
            CheckIcon()
        }

        // 텍스트를 담는 Box
        Box(
            modifier = Modifier.offset(y = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = showText,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 500)
                ) + slideInVertically(
                    initialOffsetY = { 20 },
                    animationSpec = tween(durationMillis = 500)
                )
            ) {
                Text(
                    text = "가입 완료!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                )
            }
        }
    }
}

// 전체 페이지 프리뷰
//@Preview(showBackground = true, widthDp = 360, heightDp = 640)
//@Composable
//fun SignUpCompletePagePreview() {
//    A510Theme {
//        Surface(
//            modifier = Modifier.fillMaxSize(),
//            color = MaterialTheme.colorScheme.background
//        ) {
//            SignUpCompletePage()
//        }
//    }
//}