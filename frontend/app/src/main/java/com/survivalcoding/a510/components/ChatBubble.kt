package com.survivalcoding.a510.components

import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
    name: String? = null,
    isFirstInSequence: Boolean = true,
    searchQuery: String = "",
    isImage: Boolean = false,
    imageUrl: String? = null
) {
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
                        .border(1.dp, Color.LightGray, CircleShape),
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

                MessageBubble(text, timestamp, isAiMessage, isFirstInSequence, searchQuery, isImage, imageUrl)
            }
        } else {
            MessageBubble(text, timestamp, isAiMessage, isFirstInSequence, searchQuery, isImage, imageUrl)
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
    imageUrl: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isAiMessage) 0.dp else 16.dp),
        horizontalArrangement = if (isAiMessage) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isAiMessage) {
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
                val context = LocalContext.current
                val bitmap = androidx.compose.runtime.remember(imageUrl) {
                    try {
                        val uri = Uri.parse(imageUrl)
                        val inputStream = context.contentResolver.openInputStream(uri)

                        // 비트맵 옵션을 사용하여 이미지 크기 조절
                        val options = BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                        }
                        BitmapFactory.decodeStream(inputStream, null, options)
                        inputStream?.close()

                        // 적절한 샘플 크기 계산
                        val maxSize = 1024
                        var sampleSize = 1
                        while (options.outWidth / sampleSize > maxSize ||
                            options.outHeight / sampleSize > maxSize) {
                            sampleSize *= 2
                        }

                        // 실제 비트맵 로드
                        val finalOptions = BitmapFactory.Options().apply {
                            inSampleSize = sampleSize
                        }
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream, null, finalOptions)?.asImageBitmap()
                        }
                    } catch (e: Exception) {
                        null
                    }
                }

                bitmap?.let {
                    Image(
                        bitmap = it,
                        contentDescription = "Shared image",
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
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

        if (isAiMessage) {
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