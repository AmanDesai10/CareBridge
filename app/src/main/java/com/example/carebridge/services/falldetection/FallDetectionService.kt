package com.example.carebridge.services.falldetection

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.carebridge.R
import com.example.carebridge.services.location.LocationService
import com.example.carebridge.services.location.hasLocationPermission
import com.example.carebridge.utils.LocationUtils
import com.example.carebridge.utils.SensorUtils
import com.example.carebridge.utils.mediaPlayer
import com.example.carebridge.utils.playSound
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * FallDetectionService is a service that listens to the accelerometer and gyroscope sensors to detect
 * if a fall has occurred. If a fall is detected, the service will play a sound, vibrate the device, and send a notification.
 * It will also start the LocationService to get the user's location.
 * The service will run in the background even if the app is closed.
 * The service will stop when the user stops it.
 * The service will also stop if the app is uninstalled.
 */
open class FallDetectionService : Service(), SensorEventListener {
    private lateinit var mSensorManager: SensorManager
    private var mAccelerometer: Sensor? = null
    private var mGyroscope: Sensor? = null
    private var accThreshold = 10
    private var gyroThreshold = 10
    private var lastAccelerationMagnitude = 0f
    private var lastGyroscopeMagnitude = 0f

    val notificationId = 123
    val channelId = "alert_channel"

    /**
     * onCreate is called when the service is created.
     * It initializes the [SensorManager] and the sensors [mAccelerometer] and [mGyroscope].
     * It registers the sensors to listen for sensor events.
     * It creates a notification channel and a notification to run the service in the foreground.
     * It starts the service in the foreground.
     */
    override fun onCreate() {
        super.onCreate()

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL)

        createNotificationChannel()
        val notification = createNotification()
        startForeground(notificationId, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * [onSensorChanged] is called when a sensor value changes.
     * It calculates the magnitude of the acceleration and gyroscope sensors.
     * If the difference between the current and last sensor values is greater than the threshold,
     * a fall is detected and [fallDetected] method is invoked.
     * @param event the sensor event that contains the sensor values
     * @RequiresApi(Build.VERSION_CODES.O)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    val accMagnitude = sqrt(
                        (event.values[0] * event.values[0]) +
                                (event.values[1] * event.values[1]) +
                                (event.values[2] * event.values[2])
                    )

                    if (lastAccelerationMagnitude == 0f) {
                        lastAccelerationMagnitude = accMagnitude
                    } else {
                        val accDiff = abs(lastAccelerationMagnitude - accMagnitude)
                        lastAccelerationMagnitude = accMagnitude

                        if (accDiff > accThreshold) {
                            fallDetected()
                        }
                    }
                }

                Sensor.TYPE_GYROSCOPE -> {
                    val gyroMagnitude = sqrt(
                        (event.values[0] * event.values[0]) +
                                (event.values[1] * event.values[1]) +
                                (event.values[2] * event.values[2])
                    )

                    if (lastGyroscopeMagnitude == 0f) {
                        lastGyroscopeMagnitude = gyroMagnitude
                    } else {
                        val gyroDiff = abs(lastGyroscopeMagnitude - gyroMagnitude)
                        lastGyroscopeMagnitude = gyroMagnitude

                        if (gyroDiff > gyroThreshold) {
                            fallDetected()
                        }
                    }
                }
            }
        }
    }

    /**
     * [fallDetected] is called when a fall is detected.
     * It plays a sound, vibrates the device, and sends a notification.
     * It starts the LocationService and also sends the user's location to connected family members.
     * If the location permission is not granted, a toast message is shown.
     * @RequiresApi(Build.VERSION_CODES.O)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun fallDetected() {
        playSound(this)
        SensorUtils.vibrateDevice(this)
        sendNotification(applicationContext)
        if (!LocationUtils.isLocationEnabled(this)) {
            LocationUtils.promptEnableLocation(this)
        }
        Handler().postDelayed({
            if (this.hasLocationPermission()) {
                // Start the LocationService after the delay
                val serviceIntent = Intent(this, LocationService::class.java)
                serviceIntent.action = LocationService.ACTION_START
                startForegroundService(serviceIntent)
            } else {
                // Handle the case where permissions are not granted
                Toast.makeText(
                    this,
                    "Location permissions are required to start the service",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, 10000)
    }

    /**
     * createNotificationChannel creates a notification channel for the service.
     * It is required for Android Oreo and above.
     * @RequiresApi(Build.VERSION_CODES.O)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // CreateS a notification channel (for Android Oreo and above)
            val channelName = "Alert Notifications"
            val descriptionText = "Fall Detected"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = descriptionText
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * onStartCommand is called when the service is started.
     * It returns START_STICKY to restart the service if it is killed by the system.
     * @param intent the intent that started the service
     * @param flags additional data about the start request
     * @param startId a unique integer representing the start request
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    /**
     * onDestroy is called when the service is destroyed.
     * It unregisters the sensor listener and releases the media player.
     */
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }

    /**
     * stop is called to stop the service.
     * It unregisters the sensor listener and stops the service.
     */
    fun stop() {
        mSensorManager.unregisterListener(this)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * createNotification creates a notification for the service.
     * @return the notification
     */
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Fall Detection Service")
            .setContentText("Running in the background")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()
    }
}
