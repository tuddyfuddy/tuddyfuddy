package com.survivalcoding.a510.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.survivalcoding.a510.ui.theme.BubbleTheme

@Composable
fun SpeechBubble(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                width = BubbleTheme.borderWidth,
                color = BubbleTheme.borderColor,
                shape = RoundedCornerShape(
                    topStart = BubbleTheme.cornerRadius,
                    topEnd = BubbleTheme.cornerRadius,
                    bottomStart = BubbleTheme.cornerRadius,
                    bottomEnd = 0.dp
                )
            )
            .background(
                color = BubbleTheme.backgroundColor,
                shape = RoundedCornerShape(
                    topStart = BubbleTheme.cornerRadius,
                    topEnd = BubbleTheme.cornerRadius,
                    bottomStart = BubbleTheme.cornerRadius,
                    bottomEnd = 0.dp
                )
            )
            .padding(horizontal = BubbleTheme.horizontalPadding, vertical = BubbleTheme.largeVerticalPadding)
    ) {
        Text(
            text = text,
            color = BubbleTheme.textColor,
            fontSize = BubbleTheme.largeFontSize,
            textAlign = TextAlign.Center,
            lineHeight = BubbleTheme.lineHeight
        )
    }
}
