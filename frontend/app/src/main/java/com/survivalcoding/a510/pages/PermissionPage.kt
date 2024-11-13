package com.survivalcoding.a510.pages

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.survivalcoding.a510.components.NextButton
import com.survivalcoding.a510.components.PermissionItem
import com.survivalcoding.a510.services.DailyCalendarWorker
import com.survivalcoding.a510.services.DailyWeatherWorker

@Composable
fun PermissionPage(
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentGroup by remember { mutableStateOf<List<String>?>(null) }

    // 필수 권한과 선택 권한을 분리
    val requiredPermissionGroups = remember {
        listOf(
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            listOf(Manifest.permission.RECORD_AUDIO),
            listOf(Manifest.permission.POST_NOTIFICATIONS)
        )
    }

    val optionalPermissionGroups = remember {
        listOf(
            listOf(Manifest.permission.READ_CALENDAR)
        )
    }

    val allPermissionGroups = remember {
        requiredPermissionGroups + optionalPermissionGroups
    }

    val permissionsState = remember {
        mutableStateMapOf<String, Boolean>().apply {
            allPermissionGroups.flatten().forEach { permission ->
                this[permission] = ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    // 필수 권한이 모두 승인되었는지 확인하는 함수
    fun areRequiredPermissionsGranted(): Boolean {
        return requiredPermissionGroups.all { group ->
            group.all { permission ->
                permissionsState[permission] == true
            }
        }
    }

    var currentGroupIndex by remember { mutableStateOf(0) }
    var showRequiredPermissionDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        permissionsResult.forEach { (permission, isGranted) ->
            permissionsState[permission] = isGranted
        }

        // 위치 권한이 승인되었는지 확인
        val isLocationPermissionGranted = permissionsResult[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissionsResult[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        // 위치 권한이 방금 승인되었다면 Worker 스케줄링
        if (isLocationPermissionGranted) {
            context.applicationContext?.let { appContext ->
                DailyWeatherWorker.schedule(appContext)
                Log.d("PermissionPage", "Weather Worker scheduled after location permission granted")
            }
        }

        // 캘린더 권한이 승인되었는지 확인
        val isCalendarPermissionGranted = permissionsResult[Manifest.permission.READ_CALENDAR] == true

        // 캘린더 권한이 방금 승인되었다면 Calendar Worker 스케줄링
        if (isCalendarPermissionGranted) {
            context.applicationContext?.let { appContext ->
                DailyCalendarWorker.schedule(appContext)
                Log.d("PermissionPage", "Calendar Worker scheduled after calendar permission granted")
            }
        }

        currentGroupIndex++
        if (currentGroupIndex < allPermissionGroups.size) {
            currentGroup = allPermissionGroups[currentGroupIndex]
        } else {
            if (areRequiredPermissionsGranted()) {
                onNextClick()
            } else {
                showRequiredPermissionDialog = true
                // 거부된 필수 권한이 있다면 다시 처음부터 시작
                currentGroupIndex = 0
            }
        }
    }

    LaunchedEffect(currentGroup) {
        currentGroup?.let {
            launcher.launch(it.toTypedArray())
        }
    }

    if (showRequiredPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showRequiredPermissionDialog = false },
            title = {
                Text("필수 권한 알림")
            },
            text = {
                Text("필수 권한을 모두 허용해야 서비스를 이용할 수 있습니다.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRequiredPermissionDialog = false
                        currentGroup = allPermissionGroups[0]
                    }
                ) {
                    Text("다시 시도")
                }
            }
        )
    }

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
                modifier = Modifier.padding(start = 16.dp)
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
                            tint = if (permissionsState[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                                permissionsState[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    title = "기기 위치",
                    description = "사용자 위치에 따른 맞춤 서비스",
                    isGranted = permissionsState[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                            permissionsState[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                )

                PermissionItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "마이크",
                            tint = if (permissionsState[Manifest.permission.RECORD_AUDIO] == true) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    title = "음성 녹음",
                    description = "음성 메세지 기능",
                    isGranted = permissionsState[Manifest.permission.RECORD_AUDIO] == true
                )

                PermissionItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "알림",
                            tint = if (permissionsState[Manifest.permission.POST_NOTIFICATIONS] == true) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    title = "알림",
                    description = "알림 메시지 수신",
                    isGranted = permissionsState[Manifest.permission.POST_NOTIFICATIONS] == true
                )
            }

            Column(
                modifier = Modifier.padding(start = 16.dp)
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
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "일정",
                            tint = if (permissionsState[Manifest.permission.READ_CALENDAR] == true) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    title = "일정",
                    description = "사용자 일정에 따른 맞춤 서비스",
                    isGranted = permissionsState[Manifest.permission.READ_CALENDAR] == true
                )
            }
        }

        NextButton(
            onClick = {
                currentGroupIndex = 0
                currentGroup = allPermissionGroups[0]
            }
        )
    }
}