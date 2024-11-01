package com.survivalcoding.a510.components

import android.app.Activity
import android.view.WindowManager
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TopAppBarDefaults
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onMenuClick: () -> Unit,
    title: String = "",
) {
    val view = LocalView.current
    val window = (view.context as Activity).window

    SideEffect {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = true
        }
        window.statusBarColor = Color.Transparent.toArgb()
    }

    Surface(
        shadowElevation = 4.dp,
        shape = RectangleShape
    ) {
        TopAppBar(
            modifier = modifier,  // statusBarsPadding() 제거
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "뒤로가기"
                    )
                }
            },
            title = {
                Text(
                    text = title,
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.offset(x = (-5).dp)
                )
            },
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "검색"
                    )
                }
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "메뉴"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )
    }
}