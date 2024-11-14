package com.survivalcoding.a510.utils

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun TransparentSystemBars(darkIcons: Boolean) {
    val systemUiController = rememberSystemUiController()
    val view = LocalView.current
    val window = (view.context as Activity).window

    DisposableEffect(systemUiController) {
        WindowCompat.setDecorFitsSystemWindows(window, true)

        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = darkIcons
        )

        onDispose {
            // 원래 상태로 복원
            WindowCompat.setDecorFitsSystemWindows(window, true)
            // 상태바 색상을 시스템 기본값으로 복원
            systemUiController.setStatusBarColor(
                color = Color(0xFF1C1B1F)  // 시스템 기본 색상
            )
        }
    }
}