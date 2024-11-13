package com.survivalcoding.a510.services

import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class DailyWeatherWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d(TAG, "Weather Worker started")

        // 앱이 설치되어 있는지 확인
        try {
            applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
        } catch (e: Exception) {
            Log.d(TAG, "App is uninstalled, cancelling worker")
            WorkManager.getInstance(applicationContext).cancelWorkById(id)
            return Result.failure()
        }

        // 위치 권한 체크
        if (!hasLocationPermission()) {
            Log.e(TAG, "Location permission not granted")
            return Result.failure()
        }

        return try {
            val location = getLastKnownLocation()
            if (location == null) {
                Log.e(TAG, "Failed to get location")
                return Result.failure()
            }

            Log.d(TAG, "Location obtained - Lat: ${location.latitude}, Lng: ${location.longitude}")

            // 위치 정보가 있으면 API 호출
            val response = RetrofitClient.weatherService.sendLocation(
                latitude = location.latitude.toString(),
                longitude = location.longitude.toString()
            ).execute()

            if (response.isSuccessful && response.body() != null) {
                // 응답을 String으로 변환하여 로그로 출력
                val responseBody = response.body()!!.string()
                Log.d(TAG, "Server response: $responseBody")
                schedule(applicationContext)
                Result.success()
            } else {
                Log.e(TAG, "Server returned error: ${response.code()}")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during weather worker execution", e)
            Result.failure()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return (PackageManager.PERMISSION_GRANTED ==
                applicationContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
                PackageManager.PERMISSION_GRANTED ==
                applicationContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    private fun getLastKnownLocation(): Location? {
        val locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return try {
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (location == null) {
                Log.e(TAG, "No location available from providers")
            }

            location
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception when getting location", e)
            null
        }
    }

    companion object {
        private const val TAG = "A510WeatherWorker"
        private const val UNIQUE_WORK_NAME = "daily_weather_update"

        fun schedule(context: Context) {

            val now = Calendar.getInstance()
            val nextRun = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 7)  // 오전 7시
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

            val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyWeatherWorker>()
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    UNIQUE_WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    dailyWorkRequest
                )

            Log.d(TAG, "Weather Worker scheduled successfully")
        }
    }
}