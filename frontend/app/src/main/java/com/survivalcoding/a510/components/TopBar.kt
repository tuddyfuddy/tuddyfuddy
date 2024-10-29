package com.survivalcoding.a510.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.survivalcoding.a510.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import android.app.Activity
import android.view.WindowManager
import androidx.core.view.WindowCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    val systemUiController = rememberSystemUiController()
    val view = LocalView.current
    val density = LocalDensity.current
    val statusBarHeightPx = WindowInsets.statusBars.getTop(density)
    val statusBarHeightDp = with(density) { statusBarHeightPx.toDp() }

    DisposableEffect(systemUiController) {
        val window = (view.context as Activity).window

        // 상태바 완전 투명하게 만들기 -> 그라데이션 탑바 색이 보여야함
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = false
        )
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = false
        )
    
        // TopBar가 사용되는 페이지에서만 적용해야 함으로, 탑바가 사라지면 상태바 설정 원상복귀
        onDispose {
            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            systemUiController.setStatusBarColor(Color.Transparent)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp + statusBarHeightDp)
            .drawBehind {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height * 0.7f)
                    quadraticBezierTo(
                        size.width * 0.5f,
                        size.height * 1f,
                        0f,
                        size.height * 1f
                    )
                    close()
                }

                drawIntoCanvas { canvas ->
                    canvas.drawOutline(
                        outline = Outline.Generic(path),
                        paint = Paint().apply {
                            shader = LinearGradientShader(
                                from = Offset(0f, (-statusBarHeightPx).toFloat()),
                                to = Offset(size.width, (-statusBarHeightPx).toFloat()),
                                colors = listOf(
                                    Color(0xFFD1FFB5),
                                    Color(0xFF04C628)
                                ),
                                tileMode = TileMode.Clamp
                            )
                        }
                    )
                }
            }
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
        ) {
            TopAppBar(
                modifier = Modifier.height(100.dp),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Main Logo",
                            modifier = Modifier
                                .size(85.dp)
                                .offset(y = 10.dp, x = 5.dp)
                        )

                        Spacer(modifier = Modifier.width(15.dp))

                        Text(
                            text = "Tuddy Fuddy",
                            color = Color.Blue,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.offset(y = 10.dp)
                        )
                    }
                }
            )
        }
    }
}