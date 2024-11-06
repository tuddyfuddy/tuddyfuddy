package com.survivalcoding.a510.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun UpDownButton(
    onUpClick: () -> Unit,
    onDownClick: () -> Unit,
    currentIndex: Int,
    totalResults: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
//            .background(Color(0xFFE3F2FD))
            .background(Color.White)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (totalResults > 0) "${currentIndex + 1}/$totalResults" else "검색 결과가 없습니다.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            IconButton(
                onClick = onDownClick,
                enabled = currentIndex < totalResults - 1,
                modifier = Modifier
                    .size(40.dp)
                    .border(
                        width = (0.4).dp,
                        color = Color.Black,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    modifier = Modifier
                        .size(20.dp),
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "다음 결과",
                    tint = if (currentIndex < totalResults - 1)
                        Color.Black
                    else
                        MaterialTheme.colorScheme.outline
                )
            }


            IconButton(
                onClick = onUpClick,
                enabled = currentIndex > 0,
                modifier = Modifier
                    .size(40.dp)
                    .border(
                        width = (0.4).dp,
                        color = Color.Black,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    modifier = Modifier
                        .size(20.dp),
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "이전 결과",
                    tint = if (currentIndex > 0)
                        Color.Black
                    else
                        MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}