package com.example.carebridge.utils

import android.content.Context

class SharedPreferncesManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("CareBridge", Context.MODE_PRIVATE)


    fun saveKey(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value);
        editor.apply()
    }

    fun getValue(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun removeValue(key: String) {
        val editor = sharedPreferences.edit()
        editor.remove(key)
        editor.apply()
    }
}