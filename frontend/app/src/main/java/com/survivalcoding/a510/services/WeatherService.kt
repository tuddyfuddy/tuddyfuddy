package com.survivalcoding.a510.services

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface WeatherService {
    @POST("/context/weather")
    fun sendLocation(
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String
    ): Call<ResponseBody>
}