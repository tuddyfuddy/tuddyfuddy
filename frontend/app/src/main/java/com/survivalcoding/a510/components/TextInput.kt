package com.survivalcoding.a510.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import com.survivalcoding.a510.mocks.DummyAIData


@Composable
fun TextInput(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onCameraClick: () -> Unit = {},
    onVoiceClick: () -> Unit = {},
    chatId: Int,
) {
    val chatData = remember { DummyAIData.getChatById(chatId) }

    val topBarBackgroundColor = when (chatId) {
        2, 4 -> Color(0x54E0B88A) // 연갈색
        else -> Color(0xFFE5F4FF) // 조금 더 진한 하늘색 0xFFD9EFFF
    }

    val SendIconColor = when (chatId) {
        2, 4 -> Color(0xFFF2A64E) // 연갈색
        else -> Color(0xFF1428A0) // 조금 더 진한 하늘색
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = topBarBackgroundColor)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(10.dp))

        IconButtonWithIcon(
            icon = Icons.Default.CameraAlt,
            contentDescription = "Camera Icon",
            onClick = onCameraClick
        )

        Spacer(modifier = Modifier.width(10.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .heightIn(min = 20.dp, max = 100.dp),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
            ),
            placeholder = { Text("메시지를 입력하세요") }
        )



        IconButtonWithIcon(
            icon = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send Icon",
            tint = SendIconColor,
            onClick = onSendClick
        )

        Spacer(modifier = Modifier.width(5.dp))

    }
}

@Composable
fun IconButtonWithIcon(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.Black,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(26.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}
