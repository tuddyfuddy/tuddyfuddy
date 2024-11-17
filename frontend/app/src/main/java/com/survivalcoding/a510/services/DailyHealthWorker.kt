package com.survivalcoding.a510.services

import android.content.Context
import android.util.Log
import androidx.work.*
import com.survivalcoding.a510.mocks.DummyHealthData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class DailyHealthWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Health Worker started")

        try {
            applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
        } catch (e: Exception) {
            Log.d(TAG, "App is uninstalled, cancelling worker")
            WorkManager.getInstance(applicationContext).cancelWorkById(id)
            return@withContext Result.failure()
        }

        return@withContext try {
            val healthData = DummyHealthData.healthList.first()

            val response = RetrofitClient.healthService.sendHealthData(
                HealthRequest(
                    heartRate = healthData.heartRate,
                    steps = healthData.steps,
                    sleepMinutes = healthData.sleepMinutes,
                    stressLevel = healthData.stressLevel
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()
                Log.d(TAG, "Server response: $responseBody")
                schedule(applicationContext)
                Result.success()
            } else {
                Log.e(TAG, "Server returned error: ${response.code()}")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during health worker execution", e)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "A510HealthWorker"
        private const val UNIQUE_WORK_NAME = "daily_health_update"

        fun schedule(context: Context) {
            val now = Calendar.getInstance()
            val nextRun = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)  // 다음날
                set(Calendar.HOUR_OF_DAY, 7)  // 오전 7시
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            // 7-9시 사이의 랜덤한 지연 시간 (분 단위)
            val randomDelayMinutes = Random.nextInt(0, 120) // 0-120분 (2시간)
            val initialDelay = (nextRun.timeInMillis - now.timeInMillis) +
                    (randomDelayMinutes * 60 * 1000)

            Log.d(TAG, "Scheduling next run in ${initialDelay/1000/60} minutes")

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyHealthWorker>()
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    UNIQUE_WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    dailyWorkRequest
                )

            Log.d(TAG, "Health Worker scheduled successfully")
        }
    }
}