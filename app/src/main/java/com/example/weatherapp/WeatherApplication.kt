package com.example.weatherapp

import android.app.Application

class WeatherApplication : Application() {

    // Companion object to hold the instance of this application globally
    companion object {
        lateinit var instance: WeatherApplication
            private set
    }

    // Simple in-memory cache to store app data like weather info
    private val cache = HashMap<String, Any>()

    override fun onCreate() {
        super.onCreate()
        // Initialize the instance to be accessed globally
        instance = this
    }

    // Function to put data into the cache
    fun putCache(key: String, value: Any) {
        cache[key] = value
    }

    // Function to get data from the cache using the key
    fun getCache(key: String): Any? = cache[key]
}
