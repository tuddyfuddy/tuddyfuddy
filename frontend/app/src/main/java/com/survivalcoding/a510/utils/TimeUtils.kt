package com.survivalcoding.a510.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    private val koreaTimeZone = TimeZone.getTimeZone("Asia/Seoul")
    private val chatListTimeFormat = SimpleDateFormat("a h:mm", Locale.KOREA)
    private val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale.KOREA)
    private val dateFormat = SimpleDateFormat("M월 d일", Locale.KOREA)

    private fun getCalendar(timestamp: Long): Calendar {
        return Calendar.getInstance().apply {
            timeZone = koreaTimeZone
            timeInMillis = timestamp
        }
    }

    fun formatChatTime(timestamp: Long): String {
        val calendar = getCalendar(timestamp)
        val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "오전" else "오후"
        val hour = if (calendar.get(Calendar.HOUR) == 0) 12 else calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE).toString().padStart(2, '0')

        return "$amPm $hour:$minute"
    }

    fun formatDate(timestamp: Long): String {
        val calendar = getCalendar(timestamp)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "${year}년 ${month}월 ${day}일"
    }

    fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = getCalendar(timestamp1)
        val cal2 = getCalendar(timestamp2)
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun formatChatListTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < 60000 -> "방금 전"  // 1분 이내
            diff < 3600000 -> "${diff / 60000}분 전"  // 1시간 이내
            diff < 86400000 -> chatListTimeFormat.format(Date(timestamp))  // 24시간 이내
            diff < 604800000 -> dayOfWeekFormat.format(Date(timestamp))  // 1주일 이내
            else -> dateFormat.format(Date(timestamp))  // 1주일 이상
        }
    }
}
