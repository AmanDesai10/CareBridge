package com.example.carebridge.utils

/**
 *  author: Akshat Ashish Shah
 */

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings

/**
 * LocationUtils class contains utility methods related to location services.
 */
class LocationUtils {
    companion object {
        /**
         * Checks if location services are enabled on the device.
         * @param context the context of the application
         * @return true if location services are enabled, false otherwise
         */
        fun isLocationEnabled(context: Context): Boolean {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        }

        /**
         * Prompts the user to enable location services by opening the location settings.
         * @param context the context of the application
         */
        fun promptEnableLocation(context: Context) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent)
        }
    }
}
