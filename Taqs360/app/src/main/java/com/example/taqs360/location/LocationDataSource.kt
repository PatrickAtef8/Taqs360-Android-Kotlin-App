package com.example.taqs360.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class LocationDataSource(private val context: Context) {


    suspend fun getCurrentLocation(): LocationResult {

        if (!hasLocationPermissions()) {
            return LocationResult.Failure(Exception("Location permissions not granted"))
        }

        if (!isLocationEnabled()) {
            return LocationResult.Failure(Exception("Location services are disabled. Please enable GPS or network location."))
        }

        val client: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        return try {
            val lastLocation = client.lastLocation.await()
            if (lastLocation != null) {
                return LocationResult.Success(Location(lastLocation.latitude, lastLocation.longitude))
            }
            LocationResult.Failure(Exception("Could not get location: No location data available"))
        } catch (e: SecurityException) {
            LocationResult.Failure(Exception("Location permission denied: ${e.message}"))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            LocationResult.Failure(Exception("Error fetching location: ${e.message}"))
        }
    }

    fun hasLocationPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return gpsEnabled || networkEnabled
    }
}