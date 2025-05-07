package com.example.weatherapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weatherapp.model.WeatherData

class WeatherViewModel : ViewModel() {

    // Internal mutable data holder for weather info
    private val _weatherData = MutableLiveData<WeatherData?>()

    // Exposed LiveData so UI can observe changes but not modify it directly
    val weatherData: LiveData<WeatherData?> = _weatherData

    // Updates the weather data (used when new data is fetched)
    fun setWeatherData(data: WeatherData?) {
        _weatherData.value = data
    }
}
