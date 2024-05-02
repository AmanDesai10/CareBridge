package com.example.carebridge.services.location

/**
 *  author: Akshat Ashish Shah
 */
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.carebridge.R
import com.example.carebridge.utils.AuthManager
import com.example.carebridge.utils.FCMNotificationSender
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * LocationService is a service responsible for tracking the user's location in the background.
 * It uses fused location provider client to get location updates.
 * The service can be started and stopped by sending specific intents.
 */
class LocationService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient

    /**
     * onBind method is not used in this service.
     */
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    /**
     * onCreate method initializes the location client.
     */
    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    /**
     * onStartCommand method handles the start and stop actions.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("LocationService", "Service Called")
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * start method starts the location updates and sets up the notification.
     */
    private fun start() {
        Log.d("LocationService", "Service Started")
        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Tracking location...")
            .setContentText("Location: null")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setAutoCancel(true) // Close notification when clicked
            .setOngoing(true)

        locationClient
            .getLocationUpdates(30 * 6000)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                val lat = location.latitude.toString()
                val long = location.longitude.toString()
                val updatedNotification = notification.setContentText(
                    "https://www.google.com/maps?q=$lat,$long"
                )
                val notificationIntent = Intent(Intent.ACTION_VIEW)
                notificationIntent.data = Uri.parse("https://www.google.com/maps?q=$lat,$long")
                Log.d("LocationService", "https://www.google.com/maps?q=$lat,$long")
                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val pendingIntent = PendingIntent.getActivity(
                    applicationContext,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
                val fcmNotificationSender = FCMNotificationSender()
                val authManager = AuthManager(applicationContext)

                authManager.fetchCombinedFCMTokens().addOnSuccessListener { familyMemberFCMList ->
                    for (fcm in familyMemberFCMList) {
                        fcmNotificationSender.sendNotification(
                            fcm,
                            "Last Detected Location",
                            "https://www.google.com/maps?q=$lat,$long"
                        )
                    }
                }
                notification.setContentIntent(pendingIntent) // Set the pending intent
                notificationManager.notify(1, updatedNotification.build())
            }
            .launchIn(serviceScope)

        startForeground(1, notification.build())
    }

    /**
     * stop method stops the service and removes the notification.
     */
    private fun stop() {
        stopForeground(true)
        stopSelf()
    }

    /**
     * onDestroy method cancels the service scope.
     */
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}
