package com.survivalcoding.a510.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun NextButton(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        text: String = "다음"
) {
            Button(
                onClick = onClick,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF04C628))
            ) {
                Text(text)
        }
}

@Preview(showBackground = true, name = "Next Button Preview")
@Composable
fun PreviewNextButton() {
    NextButton(onClick = {})
}
