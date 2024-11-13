package com.survivalcoding.a510.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay


@Composable
fun ThreeDotsIcon(
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp,
    dotColor: Color = MaterialTheme.colorScheme.primary,
    spacing: Dp = 4.dp
) {
    var currentDot by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while(true) {
            delay(400)
            currentDot = (currentDot + 1) % 3
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(
                        if (currentDot == index) dotColor
                        else dotColor.copy(alpha = 0.3f)
                    )
            )
        }
    }
}
