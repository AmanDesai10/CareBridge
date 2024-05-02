package com.example.carebridge.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.carebridge.services.falldetection.FallDetectionService

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Start the FallDetectionService
            val serviceIntent = Intent(context, FallDetectionService::class.java)
            context.startService(serviceIntent)
            Log.d("Boot", "onReceive: Boot completed, starting FallDetectionService")
        }
    }
}
