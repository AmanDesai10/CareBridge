package com.example.carebridge.services.location
/**
 *  author: Akshat Ashish Shah
 */
import android.location.Location
import kotlinx.coroutines.flow.Flow

/**
 * LocationClient is an interface defining methods for obtaining location updates.
 * Implementations of this interface will provide functionality for retrieving location data.
 */
interface LocationClient {

    /**
     * getLocationUpdates method is used to get updates of the user's location.
     * It returns a Flow of Location objects representing the location updates.
     * @param interval the interval between location updates in milliseconds
     * @return a Flow of Location objects
     */
    fun getLocationUpdates(interval: Long): Flow<Location>

    /**
     * LocationException is an exception class specific to location-related errors.
     * It is used to handle exceptions that may occur during location retrieval.
     * @param message a descriptive message explaining the exception
     */
    class LocationException(message: String): Exception()
}
