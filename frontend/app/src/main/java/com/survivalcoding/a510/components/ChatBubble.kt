package com.survivalcoding.a510.components

import android.content.Context
import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.survivalcoding.a510.utils.TimeUtils
import java.io.File
import androidx.compose.runtime.remember
import com.survivalcoding.a510.utils.ImageUtils
import coil.compose.AsyncImage
import coil.request.ImageRequest

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
    chatId: Int,
) {
    val dotsColor = when (chatId) {
        2, 4 -> Color(0xFFF2A64E)
        else -> Color(0xFF1428A0)
    }

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
                        fontSize = (13.5).sp,
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
                        // 점점점 표시로 변경
                        ThreeDotsIcon(
                            modifier = Modifier.padding(6.dp),
                            dotSize = 4.dp,
                            dotColor = dotsColor
                        )

                        // 동글동글 표시는 삭제
//                        CircularProgressIndicator(
//                            modifier = Modifier.size(24.dp),
//                            color = MaterialTheme.colorScheme.primary,
//                            strokeWidth = 2.dp
//                        )
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
                        showTimestamp = showTimestamp,
                        chatId = chatId // chatId 전달,
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
                showTimestamp = showTimestamp,
                chatId = chatId // chatId 전달
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
    showTimestamp: Boolean = true,
    chatId: Int,
) {
    if (text.isBlank() && !isImage && imageUrl == null) {
        return
    }

    // chatId에 따른 사용자 메시지 색상 설정
    val userMessageColor = when (chatId) {
        2, 4 -> Color(0xFFF6BD7B)
        else -> Color(0xFF8CBAF1)   // 1428A0 이게 삼성 블루 헥사코드
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
                    color = if (!isAiMessage) userMessageColor else Color.White,
                    shape = RoundedCornerShape(
                        topStart = if (!isFirstInSequence) 10.dp else if (isAiMessage) 0.dp else 10.dp,
                        topEnd = if (!isFirstInSequence) 10.dp else if (isAiMessage) 10.dp else 0.dp,
                        bottomStart = 10.dp,
                        bottomEnd = 10.dp
                    )
                )
                .background(
                    color = if (!isAiMessage) userMessageColor else Color.White,
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
                // 사용자가 보낸 이미지는 로컬 저장소에서 파일가져와서 보여주
                if (!isAiMessage) {
                    Log.d("ChatBubble", "저장한 이미지 파일 이름: $imageUrl")
                    val context = LocalContext.current
                    val directory = context.getDir("images", Context.MODE_PRIVATE)
                    val imageFile = File(directory, imageUrl)

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
                    // AI가 생성한 이미지는 URL에서 가져와서 보여주기
                    Log.d("ChatBubble", "AI 생성 이미지 URL로 로드: $imageUrl")
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Generated image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                val displayText = remember(text) {
                    text.replace(Regex("<br\\s*/*>|<br"), "\n")
                }

                HighlightedText(
                    text = displayText,  // text 대신 displayText 사용
                    searchQuery = searchQuery,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Start,
                    modifier = Modifier, // .align() 제거
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