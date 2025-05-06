package com.example.weatherapp.model

import java.io.Serializable

data class WeatherData(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentWeather,
    val forecast: List<DailyForecast>
) : Serializable

data class CurrentWeather(
    val datetime: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Double,
    val windSpeed: Double,
    val windDirection: Double,
    val conditions: String,
    val icon: String
) : Serializable

data class DailyForecast(
    val datetime: String,
    val tempMax: Double,
    val tempMin: Double,
    val conditions: String,
    val icon: String,
    val humidity: Double,
    val windSpeed: Double
) : Serializable