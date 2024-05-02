package com.example.carebridge.services.falldetection

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.carebridge.R
import com.example.carebridge.utils.AuthManager
import com.example.carebridge.utils.FCMNotificationSender
import com.example.carebridge.utils.SharedPreferncesManager

/**
 * sendNotification sends a notification to the family members of the user when a fall is detected.
 * It gets the name of the user (here the senior citizen) from shared preferences and sends a notification to the family members
 * using the FCMNotificationSender.
 * @param context the context of the activity or service
 */
fun sendNotification(context: Context) {
    // from shared preferences get name of the user
    val sharedPreferncesManager = SharedPreferncesManager(context)
    val name = sharedPreferncesManager.getValue("username") ?: "connected senior citizen"

    val title = "Fall Detected"
    val message = "A fall has been detected"
    val builder = NotificationCompat.Builder(context, FallDetectionService().channelId)
        .setSmallIcon(R.drawable.ic_launcher_background)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    // send notification to family members
    val fcmNotificationSender = FCMNotificationSender()
    val authManager = AuthManager(context)

    authManager.fetchCombinedFCMTokens().addOnSuccessListener { familyMemberFCMList ->
        for (fcm in familyMemberFCMList) {
            fcmNotificationSender.sendNotification(fcm, title + " for $name", message)
        }
    }
    with(NotificationManagerCompat.from(context)) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return@with
        } else {
            notify(FallDetectionService().notificationId, builder.build())
        }
    }
}