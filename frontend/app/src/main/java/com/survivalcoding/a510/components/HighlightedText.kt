package com.survivalcoding.a510.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun HighlightedText(
    text: String,
    searchQuery: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = 12.sp,
    lineHeight: TextUnit = 20.sp
) {
    if (searchQuery.isEmpty()) {
        Text(
            text = text,
            modifier = modifier,
            textAlign = textAlign,
            color = color,
            fontSize = fontSize,
            lineHeight = lineHeight
        )
        return
    }

    val annotatedString = buildAnnotatedString {
        var currentIndex = 0
        val lowerCaseText = text.lowercase()
        val lowerCaseQuery = searchQuery.lowercase()

        while (currentIndex < text.length) {
            val startIndex = lowerCaseText.indexOf(lowerCaseQuery, currentIndex)
            if (startIndex == -1) {
                // 남은 텍스트에서 검색어를 찾지 못한 경우
                append(text.substring(currentIndex))
                break
            }

            // 검색어 이전 텍스트 추가
            append(text.substring(currentIndex, startIndex))

            // 검색어에 해당하는 부분을 하이라이트 처리
            withStyle(
                style = SpanStyle(
                    background = Color.Black,
                    color = Color.White
                )
            ) {
                append(text.substring(startIndex, startIndex + searchQuery.length))
            }

            currentIndex = startIndex + searchQuery.length
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        textAlign = textAlign,
        color = color,
        fontSize = fontSize,
        lineHeight = lineHeight
    )
}