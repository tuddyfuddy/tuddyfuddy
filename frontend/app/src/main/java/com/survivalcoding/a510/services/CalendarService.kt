package com.survivalcoding.a510.services

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CalendarService {
    @GET("/context/calendar")
    fun sendCalendarTitle(
        @Query("title") title: String
    ): Call<ResponseBody>
}