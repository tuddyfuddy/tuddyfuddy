package com.survivalcoding.a510.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.survivalcoding.a510.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colorScheme.surface.luminance() > 0.5

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color(0xFF90EE90),
            darkIcons = useDarkIcons
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .drawBehind {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0.5f)
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
                                from = Offset(size.width / 2, 0f),
                                to = Offset(size.width / 2, size.height),
                                colors = listOf(
                                    Color(0xFF90EE90),
                                    Color(0xFF98FB98)
                                )
                            )
                        }
                    )
                }
            }
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
                            .size(70.dp)
                            .offset(y = 20.dp, x = 10.dp)
                    )

                    Spacer(modifier = Modifier.width(150.dp))

                    Text(
                        text = "Tuddy Fuddy",
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.offset(y = 20.dp)
                    )
                }
            }
        )
    }
}