package com.example.carebridge.utils

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Service responsible for handling notifications received from Firebase Cloud Messaging (FCM).
 * This service extends FirebaseMessagingService and overrides its methods to handle incoming
 * messages and token refresh events.
 */
class NotificationService : FirebaseMessagingService() {

    /**
     * Method called when a new message is received from FCM.
     * @param remoteMessage The received message.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Received message from: ${remoteMessage.from}")

        // Check if the message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Notification Message Body: ${it.body}")
            if (it.body != null && it.body.toString().contains("https://www.google.com/maps")) {
                Log.d(TAG, "Map link: ${it.body}")

            }
        }
    }

    /**
     * Method called when the FCM token is refreshed.
     * @param token The new FCM token.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}
