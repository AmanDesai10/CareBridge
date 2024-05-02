package com.example.carebridge.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

/**
 * BroadcastReceiver responsible for receiving reminder notifications and sending them to the appropriate FCM tokens.
 * This receiver is triggered by the Android system when a reminder alarm is activated.
 */
class ReminderReceiver : BroadcastReceiver() {
    private lateinit var authManager: AuthManager

    /**
     * Method called when the BroadcastReceiver receives a broadcast intent.
     * @param context The context in which the receiver is running.
     * @param intent The Intent being received.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            authManager = AuthManager(context)
            val fcmNotificationSender = FCMNotificationSender()
            val message = intent?.getStringExtra("message") ?: "Message"
            val title = intent?.getStringExtra("title") ?: "Title"
            Log.d("inside this", "inside")

            // Fetch FCM tokens of family members and send reminder notification to each token
            authManager.fetchCombinedFCMTokens().addOnSuccessListener { familyMemberFCMList ->
                for(fcm in familyMemberFCMList) {
                    if(fcm != null && fcm != "") {
                        fcmNotificationSender.sendNotification(fcm, title, message)
                    }
                }
            }

            // Fetch FCM token of current user and send reminder notification
            authManager.fetchFCMTokenFromCurrentUser() {fcmToken ->
                Log.d("inside token api", fcmToken.toString())

                if(fcmToken != null && fcmToken != "") {
                    fcmNotificationSender.sendNotification(fcmToken, title, message)
                }
            }
        }
    }
}
