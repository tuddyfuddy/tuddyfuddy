package com.survivalcoding.a510.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.survivalcoding.a510.utils.TimeUtils
import java.io.File
import androidx.compose.runtime.remember
import com.survivalcoding.a510.utils.ImageUtils

@Composable
fun ChatBubble(
    text: String,
    timestamp: Long,
    modifier: Modifier = Modifier,
    isAiMessage: Boolean = false,
    profileImage: Int? = null,
    name: String? = null,
    isFirstInSequence: Boolean = true,
    searchQuery: String = "",
    isImage: Boolean = false,
    imageUrl: String? = null,
    showTimestamp: Boolean = true,
    isLoading: Boolean = false,
) {
    if (!isLoading && text.isBlank() && !isImage && imageUrl == null) {
        return
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isAiMessage) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.Top
    ) {
        if (isAiMessage) {
            if (profileImage != null) {
                Image(
                    painter = painterResource(id = profileImage),
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(0.dp, Color.LightGray, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Spacer(modifier = Modifier.size(38.dp))
            }
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

                if (isLoading) {
                    // 로딩 아이콘 표시
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    }
                } else {
                    // 기존 MessageBubble 표시
                    MessageBubble(
                        text = text,
                        timestamp = timestamp,
                        isAiMessage = isAiMessage,
                        isFirstInSequence = isFirstInSequence,
                        searchQuery = searchQuery,
                        isImage = isImage,
                        imageUrl = imageUrl,
                        showTimestamp = showTimestamp
                    )
                }
            }
        } else {
            // 사용자 메시지는 로딩 상태가 없으므로 그대로 유지
            MessageBubble(
                text = text,
                timestamp = timestamp,
                isAiMessage = isAiMessage,
                isFirstInSequence = isFirstInSequence,
                searchQuery = searchQuery,
                isImage = isImage,
                imageUrl = imageUrl,
                showTimestamp = showTimestamp
            )
        }
    }
}


@Composable
fun MessageBubble(
    text: String,
    timestamp: Long,
    isAiMessage: Boolean,
    isFirstInSequence: Boolean,
    searchQuery: String = "",
    isImage: Boolean = false,
    imageUrl: String? = null,
    showTimestamp: Boolean = true
) {
    if (text.isBlank() && !isImage && imageUrl == null) {
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isAiMessage) 0.dp else 16.dp),
        horizontalArrangement = if (isAiMessage) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isAiMessage && showTimestamp) {
            Text(
                text = TimeUtils.formatChatTime(timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Bottom),
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = if (isImage) 200.dp else 220.dp)
                .border(
                    width = 0.05.dp,
                    color = if (!isAiMessage) Color.Yellow else Color.White,
                    shape = RoundedCornerShape(
                        topStart = if (!isFirstInSequence) 10.dp else if (isAiMessage) 0.dp else 10.dp,
                        topEnd = if (!isFirstInSequence) 10.dp else if (isAiMessage) 10.dp else 0.dp,
                        bottomStart = 10.dp,
                        bottomEnd = 10.dp
                    )
                )
                .background(
                    color = if (!isAiMessage) Color.Yellow else Color.White,
                    shape = RoundedCornerShape(
                        topStart = if (!isFirstInSequence) 10.dp else if (isAiMessage) 0.dp else 10.dp,
                        topEnd = if (!isFirstInSequence) 10.dp else if (isAiMessage) 10.dp else 0.dp,
                        bottomStart = 10.dp,
                        bottomEnd = 10.dp
                    )
                )
                .padding(
                    horizontal = if (isImage) 0.dp else 10.dp,
                    vertical = if (isImage) 0.dp else 7.dp
                )
        ) {
            if (isImage && imageUrl != null) {

                Log.d("ChatBubble", "저장한 이미지 파일 이름: $imageUrl")

                    val context = LocalContext.current
                    val directory = context.getDir("images", Context.MODE_PRIVATE)
                    val imageFile = File(directory, imageUrl)  // 파일 이름으로 전체 경로 생성

                    Log.d("ChatBubble", "이미지 절대 경로: ${imageFile.absolutePath}")
                    Log.d("ChatBubble", "이미지 파일 있는지 체크하는거: ${imageFile.exists()}")


                if (imageFile.exists()) {
                    val bitmap = remember(imageUrl) {
                        ImageUtils.loadAndRotateImage(imageFile)
                    }

                    bitmap?.let {
                        Image(
                            bitmap = it,
                            contentDescription = "Shared image",
                            modifier = Modifier
                                .widthIn(max = 800.dp)
                                .heightIn(max = 800.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Inside
                        )
                    }
                }
            } else {
                HighlightedText(
                    text = text,
                    searchQuery = searchQuery,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.align(Alignment.CenterStart),
                    fontSize = 12.sp
                )
            }
        }

        if (isAiMessage && showTimestamp) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = TimeUtils.formatChatTime(timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Bottom)
            )
        }
    }
}