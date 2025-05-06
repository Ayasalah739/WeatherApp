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
import androidx.core.app.ActivityCompat

class LocationService : Service(), LocationListener {

    private lateinit var locationManager: LocationManager
    private var locationCallback: ((Location) -> Unit)? = null

    fun startListening(context: Context, callback: (Location) -> Unit) {
        locationCallback = callback
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            callback(Location(LocationManager.GPS_PROVIDER).apply {
                latitude = 0.0
                longitude = 0.0
            })
            return
        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            10000L,
            10f,
            this
        )
    }

    fun stopListening() {
        locationManager.removeUpdates(this)
        locationCallback = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onLocationChanged(location: Location) {
        locationCallback?.invoke(location)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}