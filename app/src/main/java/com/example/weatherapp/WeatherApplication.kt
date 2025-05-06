package com.example.weatherapp

import android.app.Application

class WeatherApplication : Application() {
    companion object {
        lateinit var instance: WeatherApplication
            private set
    }

    private val cache = HashMap<String, Any>()

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun putCache(key: String, value: Any) {
        cache[key] = value
    }

    fun getCache(key: String): Any? = cache[key]
}