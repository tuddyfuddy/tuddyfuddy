package com.survivalcoding.a510.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.survivalcoding.a510.components.NextButton
import com.survivalcoding.a510.components.PermissionItem

@Composable
fun PermissionPage(
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "다음 권한이 필요합니다",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 30.dp, bottom = 30.dp)
            )

            Column(
                modifier = Modifier.padding(start = 16.dp)  // 들여쓰기 추가
            ) {
                Text(
                    text = "필수 접근 권한",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                PermissionItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "위치",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    title = "기기 위치",
                    description = "사용자 위치에 따른 맞춤 서비스"
                )

                PermissionItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "마이크",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    title = "음성 녹음",
                    description = "음성 메세지 기능"
                )

                PermissionItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "알림",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    title = "알림",
                    description = "알림 메시지 수신"
                )
            }

            Column(
                modifier = Modifier.padding(start = 16.dp)  // 선택 접근 권한 섹션도 동일하게 들여쓰기
            ) {
                Text(
                    text = "선택 접근 권한",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )

                PermissionItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "생체인식",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    title = "생체인식",
                    description = "사용자 생체인식(지문 패턴 시 개인정보)"
                )

                PermissionItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "일정",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    title = "일정",
                    description = "사용자 일정에 따른 맞춤 서비스"
                )
            }
        }

        NextButton(
            onClick = onNextClick,
        )
    }
}

@Preview(
    name = "Permission Page Preview",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PermissionPagePreview() {
    MaterialTheme {
        PermissionPage(
            onNextClick = { }
        )
    }
}