package com.survivalcoding.a510.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun CheckIcon(
    modifier: Modifier = Modifier,
    circleColor: Color = Color(0xFF00C853),
    checkColor: Color = Color.White,
    circleSize: Float = 60f
) {
    // 애니메이션 실행 여부를 추적하는 상태값
    var animationPlayed by remember { mutableStateOf(false) }

    // 원 애니메이션을 위한 Animatable (0f -> 1f)
    val circleAnimatable = remember { Animatable(0f) }

    // 체크마크 애니메이션을 위한 Animatable (0f -> 1f)
    val checkAnimatable = remember { Animatable(0f) }

    // 컴포넌트가 처음 표시될 때 한 번만 실행되는 애니메이션
    LaunchedEffect(key1 = true) {
        animationPlayed = true

        // 1단계: 원 애니메이션
        circleAnimatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            )
        )

        // 2단계: 체크마크 애니메이션
        checkAnimatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 700,
                easing = FastOutSlowInEasing
            )
        )
    }

    // 실제 그리기 작업
    Canvas(
        modifier = modifier.size(circleSize.dp)
    ) {
        val center = Offset(size.width / 2, size.height / 2)  // 중심점 계산
        val radius = size.width / 2                           // 반지름 계산

        // 원 그리기 (애니메이션 진행도에 따라 크기가 커짐)
        drawCircle(
            color = circleColor,
            radius = radius * circleAnimatable.value,
            center = center
        )

        // 체크마크를 그리기 위한 좌표 계산
        val checkStartX = center.x - radius * 0.45f
        val checkMiddleX = center.x - radius * 0.05f
        val checkEndX = center.x + radius * 0.45f

        val checkStartY = center.y
        val checkMiddleY = center.y + radius * 0.35f
        val checkEndY = center.y - radius * 0.35f

        // 원 애니메이션이 완료된 후 체크마크 그리기
        if (circleAnimatable.value == 1f) {
            // 체크마크의 첫 번째 선 (왼쪽 아래 방향)
            val firstLineProgress = (checkAnimatable.value * 1.5f).coerceAtMost(1f)

            if (firstLineProgress > 0f) {
                drawLine(
                    color = checkColor,
                    start = Offset(checkStartX, checkStartY),
                    end = Offset(
                        x = checkStartX + (checkMiddleX - checkStartX) * firstLineProgress,
                        y = checkStartY + (checkMiddleY - checkStartY) * firstLineProgress
                    ),
                    strokeWidth = 14f,
                    cap = StrokeCap.Round
                )
            }

            // 체크마크의 두 번째 선 (오른쪽 위 방향)
            val secondLineProgress = ((checkAnimatable.value * 1.5f) - 0.5f).coerceIn(0f, 1f)

            if (secondLineProgress > 0f) {
                drawLine(
                    color = checkColor,
                    start = Offset(checkMiddleX, checkMiddleY),
                    end = Offset(
                        x = checkMiddleX + (checkEndX - checkMiddleX) * secondLineProgress,
                        y = checkMiddleY + (checkEndY - checkMiddleY) * secondLineProgress
                    ),
                    strokeWidth = 14f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}