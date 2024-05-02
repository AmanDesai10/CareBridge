package com.example.carebridge.services.location
/**
 *  author: Akshat Ashish Shah
 */
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Extension function to check if the app has location permissions.
 * @return true if the app has location permissions, false otherwise
 */
fun Context.hasLocationPermission(): Boolean {
    // Check if both ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION permissions are granted
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}
