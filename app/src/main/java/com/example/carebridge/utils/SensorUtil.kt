package com.example.carebridge.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * SensorUtils is a utility class that provides helper functions for sensors.
 */
object SensorUtils {

    /**
     * vibrateDevice vibrates the device for 500 milliseconds.
     * @param context the context of the activity or service
     * @throws SecurityException if the app does not have the VIBRATE permission
     * @throws UnsupportedOperationException if the device does not support vibration
     */
    fun vibrateDevice(context: Context) {
        // Gets the vibrator service
        val vibrator: Vibrator by lazy {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
        }

        // for Android 8.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }

    }

}