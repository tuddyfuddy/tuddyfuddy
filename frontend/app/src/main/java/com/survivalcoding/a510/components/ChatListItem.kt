package com.survivalcoding.a510.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color


@Composable
fun ChatListItem(
    modifier: Modifier = Modifier,
    profileImage: Int,  // 프로필 이미지 ID
    name: String,       // 사용자 이름
    message: String,    // 메시지 내용
    timestamp: String,  // 메세지 도착 시간
    unreadCount: Int = 0, // 안읽은 메세지 개수
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = profileImage),
            contentDescription = "Profile Image of $name",
            modifier = Modifier
                .size(70.dp)
                .padding(end = 16.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = timestamp,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (message.length > 19) {
                        message.take(19) + "..."
                    } else {
                        message
                    },
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
//                            .size(18.dp)
                            .height(20.dp)
                            .width(30.dp)
                            .background(
                                color = Color(0xFF04C628),
                                shape = RoundedCornerShape(10.dp)
//                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "new",
//                            text = unreadCount.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.offset(y = (-2.5).dp, x = (0).dp)
                        )
                    }
                }
            }
        }
    }
}