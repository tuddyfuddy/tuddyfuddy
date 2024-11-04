package com.survivalcoding.a510.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.survivalcoding.a510.utils.TimeUtils

@Composable
fun ChatBubble(
    text: String,
    timestamp: Long,
    modifier: Modifier = Modifier,
    isAiMessage: Boolean = false,
    profileImage: Int? = null,
    name: String? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isAiMessage) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.Top
    ) {
        if (isAiMessage && profileImage != null) {
            Image(
                painter = painterResource(id = profileImage),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.LightGray, CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                name?.let {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        color = Color.Black
                    )
                }

                MessageBubble(text, timestamp, isAiMessage)
            }
        } else {
            MessageBubble(text, timestamp, isAiMessage)
        }
    }
}

@Composable
fun MessageBubble(text: String, timestamp: Long, isAiMessage: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isAiMessage) 0.dp else 16.dp),
        horizontalArrangement = if (isAiMessage) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isAiMessage) {  // 내 메세지면 타임스탬프를 왼쪽에 표시하기
            Text(
                text = TimeUtils.formatChatTime(timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Bottom)
            )
            Spacer(modifier = Modifier.width(4.dp))  // 타임스탬프와 말풍선 간격
        }

        Box(
            modifier = Modifier
                .widthIn(max = 220.dp)
                .border(
                    width = 0.05.dp,
                    color = if (!isAiMessage) Color.Yellow else Color.White,
                    shape = RoundedCornerShape(
                        topStart = if (isAiMessage) 0.dp else 10.dp,
                        topEnd = if (isAiMessage) 10.dp else 0.dp,
                        bottomStart = 10.dp,
                        bottomEnd = 10.dp
                    )
                )
                .background(
                    color = if (!isAiMessage) Color.Yellow else Color.White,
                    shape = RoundedCornerShape(
                        topStart = if (isAiMessage) 0.dp else 10.dp,
                        topEnd = if (isAiMessage) 10.dp else 0.dp,
                        bottomStart = 10.dp,
                        bottomEnd = 10.dp
                    )
                )
                .padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Text(
                text = text,
                color = Color.DarkGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Start,
                lineHeight = 20.sp
            )
        }

        // AI 메시지에도 항상 타임스탬프가 표시되도록 조건 없이 표시
        if (isAiMessage) {
            Spacer(modifier = Modifier.width(4.dp))  // 말풍선과 타임스탬프 사이의 간격
            Text(
                text = TimeUtils.formatChatTime(timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Bottom)
            )
        }
    }
}
