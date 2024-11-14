package com.survivalcoding.a510.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.survivalcoding.a510.R


@Composable
fun ChatListBottom(
    name: String,
    description: String,
    characterImageRes: Int,
    modifier: Modifier = Modifier
) {

    val notoSansKr = FontFamily(
        Font(R.font.notosanskr_thin, FontWeight.Thin),
        Font(R.font.notosanskr_light, FontWeight.Light),
        Font(R.font.notosanskr_regular, FontWeight.Normal),
        Font(R.font.notosanskr_medium, FontWeight.Medium),
        Font(R.font.notosanskr_bold, FontWeight.Bold),
        Font(R.font.notosanskr_black, FontWeight.Black)
    )

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
                        style = MaterialTheme.typography.headlineLarge
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        modifier = Modifier.offset(x = 3.dp),
                        text = description,
                        style = MaterialTheme.typography.bodyMedium
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