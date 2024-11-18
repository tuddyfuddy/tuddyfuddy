package com.survivalcoding.a510.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.survivalcoding.a510.R

@Composable
fun CircleCharacter(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.circlecha),
        contentDescription = "Circle Character",
        modifier = modifier
            .size(70.dp)
            .clickable(onClick = onClick)
    )
}
