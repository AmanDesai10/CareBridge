package com.example.carebridge.utils

import android.content.Context
import android.content.SharedPreferences

class AppLaunchChecker(private val context: Context) {
    private val sharedPreferences = context.applicationContext.getSharedPreferences(
        SHARED_PREF_NAME,
        Context.MODE_PRIVATE
    )

    fun isFirstLaunch(): Boolean {
        val isFirstLaunch = sharedPreferences.getBoolean(FIRST_LAUNCH_KEY, true)
        if (isFirstLaunch) {
            // Update the flag to indicate that the app has been launched
            sharedPreferences.edit().putBoolean(FIRST_LAUNCH_KEY, false).apply()
        }
        return isFirstLaunch
    }

    companion object {
        private const val SHARED_PREF_NAME = "MyAppPrefs"
        private const val FIRST_LAUNCH_KEY = "first_launch"
    }
}