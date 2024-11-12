package com.survivalcoding.a510.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.util.Log
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class DailyCalendarWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d(TAG, "Calendar Worker started")

        // 앱이 설치되어 있는지 확인
        try {
            applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
        } catch (e: Exception) {
            Log.d(TAG, "App is uninstalled, cancelling worker")
            WorkManager.getInstance(applicationContext).cancelWorkById(id)
            return Result.failure()
        }

        // 캘린더 권한 체크
        if (!hasCalendarPermission()) {
            Log.e(TAG, "Calendar permission not granted")
            return Result.failure()
        }

        return try {
            val titles = getTodayCalendarTitles()
            if (titles.isEmpty()) {
                Log.d(TAG, "No calendar events found for today")
                schedule(applicationContext)
                return Result.success()
            }

            Log.d(TAG, "Found ${titles.size} calendar events for today")

            // 각 일정 제목을 서버로 전송
            titles.forEach { title ->
                val response = RetrofitClient.calendarService.sendCalendarTitle(
                    title = title
                ).execute()

                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!.string()
                    Log.d(TAG, "Server response for title '$title': $responseBody")
                } else {
                    Log.e(TAG, "Server returned error for title '$title': ${response.code()}")
                }
            }

            schedule(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during calendar worker execution", e)
            Result.failure()
        }
    }

    private fun hasCalendarPermission(): Boolean {
        return PackageManager.PERMISSION_GRANTED ==
                applicationContext.checkSelfPermission(Manifest.permission.READ_CALENDAR)
    }

    private fun getTodayCalendarTitles(): List<String> {
        val titles = mutableListOf<String>()

        // 오늘 날짜의 시작과 끝 시간 계산
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} < ?"
        val selectionArgs = arrayOf(today.timeInMillis.toString(), tomorrow.timeInMillis.toString())

        try {
            applicationContext.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                arrayOf(CalendarContract.Events.TITLE),
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    cursor.getString(0)?.let { title ->
                        titles.add(title)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading calendar events", e)
        }

        return titles
    }

    companion object {
        private const val TAG = "A510CalendarWorker"
        private const val UNIQUE_WORK_NAME = "daily_calendar_update"

        fun schedule(context: Context) {
            val now = Calendar.getInstance()
            val nextRun = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 7)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            // 7-9시 사이의 랜덤한 지연 시간 (분 단위)
            val randomDelayMinutes = Random.nextInt(0, 120) // 0-120분
            val initialDelay = (nextRun.timeInMillis - now.timeInMillis) +
                    (randomDelayMinutes * 60 * 1000)

            Log.d(TAG, "Scheduling next run in ${initialDelay/1000/60} minutes")

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyCalendarWorker>()
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    UNIQUE_WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    dailyWorkRequest
                )

            Log.d(TAG, "Calendar Worker scheduled successfully")
        }
    }
}