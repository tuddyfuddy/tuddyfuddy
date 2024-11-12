package com.survivalcoding.a510.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ChatListBottom(
    name: String,
    description: String,
    characterImageRes: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        Row {
            Box(
                modifier = Modifier.offset(y = (70).dp, x = 30.dp)
            ) {
                Column {
                    Text(
                        text = name,
                        fontSize = 30.sp,
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = description,
                        fontSize = 14.sp,
                        modifier = Modifier.offset(x = 3.dp)
                    )
                }
            }
            Box(
                modifier = Modifier.offset(y = 40.dp, x = (-5).dp)
            ) {
                Image(
                    painter = painterResource(id = characterImageRes),
                    contentDescription = "$name Character Image",
                    modifier = Modifier.size(300.dp)

                )
            }
        }
    }
}