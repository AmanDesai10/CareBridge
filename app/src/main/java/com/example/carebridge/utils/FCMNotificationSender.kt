package com.example.carebridge.utils

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/**
 * FCMNotificationSender class responsible for sending notifications via FCM (Firebase Cloud Messaging).
 */
class FCMNotificationSender {
    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    val SERVER_KEY = "<SERVER KEY>"
    var client: OkHttpClient = OkHttpClient()

    /**
     * Private function to log error messages.
     */
    private fun logError(message: String) {
        println("Error: $message") // For testing purposes, print the error message
    }

    /**
     * Function to send a notification via FCM.
     * @param deviceToken The token of the device to receive the notification.
     * @param title The title of the notification.
     * @param message The body message of the notification.
     */
    fun sendNotification(deviceToken: String, title: String, message: String) {
        try {
            val client = OkHttpClient()
            val JSON = "application/json; charset=utf-8".toMediaType()

            // Create JSON body for the notification
            val body = JSONObject()
            body.put("to", deviceToken)
            body.put("priority", "high")

            val notification = JSONObject()
            notification.put("title", title)
            notification.put("body", message)

            body.put("notification", notification)

            // Create HTTP request
            val request = Request.Builder()
                .url(FCM_API)
                .addHeader("Authorization", "key=$SERVER_KEY")
                .addHeader("Content-Type", "application/json")
                .post(body.toString().toRequestBody(JSON))
                .build()

            // Asynchronous call to send the notification
            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    Log.i("FCMNotificationSender", response.body?.string() ?: "")
                }

                override fun onFailure(call: Call, e: IOException) {
                    Log.e("FCMNotificationSender", "Request failed: ${e.message}")
                }
            })
        } catch (e: IOException) {
            // Handle IO Exception
            Log.e("FCMNotificationSender", "IO Exception: ${e.message}")
        } catch (e: Exception) {
            // Log other exceptions
            logError(e.message.toString())
        }
    }
}
