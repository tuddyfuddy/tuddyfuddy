package com.survivalcoding.a510.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.survivalcoding.a510.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
//    TransparentSystemBars(darkIcons = true)

    val density = LocalDensity.current
    val statusBarHeightDp = with(density) { WindowInsets.statusBars.getTop(density).toDp() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp + statusBarHeightDp)
            .drawBehind {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height * 0.7f)
                    quadraticTo(
                        size.width * 0.5f,
                        size.height,
                        0f,
                        size.height
                    )
                    close()
                }
                drawIntoCanvas { canvas ->
                    canvas.drawOutline(
                        outline = Outline.Generic(path),
                        paint = Paint().apply {
                            shader = LinearGradientShader(
                                from = Offset(0f, -statusBarHeightDp.toPx()),
                                to = Offset(size.width, -statusBarHeightDp.toPx()),
                                colors = listOf(Color(0xFFD1FFB5), Color(0xFF04C628)),
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
