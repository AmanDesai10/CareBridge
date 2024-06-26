package com.example.carebridge.services.location
/**
 *  author: Akshat Ashish Shah
 */
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

/**
 * DefaultLocationClient is an implementation of the LocationClient interface.
 * It uses the fused location provider client to retrieve location updates.
 */
class DefaultLocationClient(
    private val context: Context,
    private val client: FusedLocationProviderClient
): LocationClient {

    /**
     * getLocationUpdates method retrieves location updates using the fused location provider client.
     * It returns a Flow of Location objects representing the location updates.
     * @param interval the interval between location updates in milliseconds
     * @return a Flow of Location objects
     * @throws LocationException if there is a problem with location permissions or GPS is disabled
     */
    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long): Flow<Location> {
        return callbackFlow {
            if(!context.hasLocationPermission()) {
                throw LocationClient.LocationException("Missing location permission")
            }

            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if(!isGpsEnabled && !isNetworkEnabled) {
                throw LocationClient.LocationException("GPS is disabled")
            }

            val request = LocationRequest.create()
                .setInterval(interval)
                .setFastestInterval(interval)

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    result.locations.lastOrNull()?.let { location ->
                        // Send the latest location update to the flow
                        launch { send(location) }
                    }
                }
            }

            // Request location updates
            client.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )

            // Close the flow when cancelled
            awaitClose {
                client.removeLocationUpdates(locationCallback)
            }
        }
    }
}
