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


@Composable
fun TextInput(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onCameraClick: () -> Unit = {},
    onVoiceClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color(0xFFE7EBFF))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButtonWithIcon(
            icon = Icons.Default.CameraAlt,
            contentDescription = "Camera Icon",
            onClick = onCameraClick
        )

        Spacer(modifier = Modifier.width(10.dp))

        IconButtonWithIcon(
            icon = Icons.Default.KeyboardVoice,
            contentDescription = "Voice Icon",
            onClick = onVoiceClick
        )

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
            tint = Color.Blue,
            onClick = onSendClick
        )
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
