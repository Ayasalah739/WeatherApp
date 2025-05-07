package com.example.weatherapp.service

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat

class LocationService : Service(), LocationListener {

    private var locationManager: LocationManager? = null
    private var locationCallback: ((Location) -> Unit)? = null

    // Starts location tracking using GPS provider
    fun startListening(context: Context, callback: (Location) -> Unit) {
        stopListening() // Clean up any previous listeners
        locationCallback = callback
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Check for location permission
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("LocationService", "Location permission not granted")

            // If permission is missing, return dummy location
            callback(Location(LocationManager.GPS_PROVIDER).apply {
                latitude = 0.0
                longitude = 0.0
            })
            return
        }

        try {
            // Request location updates every 10 seconds, with 10 meters change
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                10000L,
                10f,
                this
            )
            Log.d("LocationService", "Started listening for location updates")
        } catch (e: Exception) {
            Log.e("LocationService", "Failed to request location updates: ${e.message}")

            // Fallback in case of error
            callback(Location(LocationManager.GPS_PROVIDER).apply {
                latitude = 0.0
                longitude = 0.0
            })
        }
    }

    // Stops location updates and clears callback
    fun stopListening() {
        try {
            locationManager?.removeUpdates(this)
            Log.d("LocationService", "Stopped listening for location updates")
        } catch (e: Exception) {
            Log.e("LocationService", "Failed to remove location updates: ${e.message}")
        }
        locationCallback = null
    }

    // Required override; not used since we don’t bind the service
    override fun onBind(intent: Intent?): IBinder? = null

    // Called when location changes
    override fun onLocationChanged(location: Location) {
        Log.d("LocationService", "Location changed: ${location.latitude}, ${location.longitude}")
        locationCallback?.invoke(location)
    }

    // These are unused but must be overridden when implementing LocationListener
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}
