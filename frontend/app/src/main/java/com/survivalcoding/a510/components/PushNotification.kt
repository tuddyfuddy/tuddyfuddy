package com.survivalcoding.a510.components

import android.Manifest
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.survivalcoding.a510.R
import com.survivalcoding.a510.notification.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class NotificationData(
    val title: String,
    val message: String
)

@Composable
fun PushNotification(
    notification: NotificationData?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var animationState by remember { mutableStateOf(-1f) }
    var offsetYValue by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    val offsetY by animateFloatAsState(
        targetValue = when(animationState) {
            -1f -> -200f
            0f -> offsetYValue
            else -> -200f
        },
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "offsetY"
    )

    LaunchedEffect(notification) {
        if (notification != null) {
            offsetYValue = 0f
            animationState = -1f
            delay(50)
            animationState = 0f
            delay(3000)
            animationState = -1f
            delay(300)
            onDismiss()
        }
    }

    if (notification != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f)
        ) {
            Box(
                modifier = Modifier
                    .offset(y = offsetY.dp)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (offsetYValue < -50f) {
                                    animationState = -1f
                                    coroutineScope.launch {
                                        delay(300)
                                        onDismiss()
                                    }
                                } else {
                                    offsetYValue = 0f
                                }
                            },
                            onDragCancel = {
                                offsetYValue = 0f
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                if (dragAmount < 0 || offsetYValue < 0) {
                                    offsetYValue += dragAmount
                                    offsetYValue = offsetYValue.coerceIn(-200f, 0f)
                                }
                            }
                        )
                    }
            ) {
                NotificationContent(notification = notification)
            }
        }
    }
}

@Composable
private fun NotificationContent(
    notification: NotificationData
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
    ) {
        Card(
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.Center),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "로고",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 8.dp)
                )

                Text(
                    text = notification.title,
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Text(
                    text = notification.message,
                    color = Color.Black.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        }
    }
}

@Preview(
    name = "알림 테스트 화면",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun NotificationScreenPreview() {
    var currentNotification by remember { mutableStateOf<NotificationData?>(null) }
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }

    // Android 13 이상에서 알림 권한 요청
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(context, permission) !=
                PackageManager.PERMISSION_GRANTED) {
                (context as? ComponentActivity)?.requestPermissions(
                    arrayOf(permission),
                    1
                )
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        PushNotification(
            notification = currentNotification,
            onDismiss = { currentNotification = null }
        )

        Button(
            onClick = {
                val notification = NotificationData(
                    title = "박명수",
                    message = "안녕하세요! 새로운 메시지가 도착했습니다."
                )
                currentNotification = notification
                notificationHelper.showNotification(notification)
            },
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text("알림 테스트")
        }
    }
}