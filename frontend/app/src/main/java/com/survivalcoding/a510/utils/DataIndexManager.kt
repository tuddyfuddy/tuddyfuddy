package com.survivalcoding.a510.utils

import android.content.Context
import android.content.SharedPreferences

object DataIndexManager {
    private const val PREF_NAME = "DataIndexManager"
    private const val KEY_CURRENT_INDEX = "current_index"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getNextIndex(maxSize: Int): Int {
        val currentIndex = prefs.getInt(KEY_CURRENT_INDEX, 0)
        val nextIndex = (currentIndex + 1) % maxSize

        prefs.edit().putInt(KEY_CURRENT_INDEX, nextIndex).apply()

        return currentIndex
    }
}