package com.example.weatherapp

import com.example.weatherapp.model.CurrentWeather
import com.example.weatherapp.model.DailyForecast
import com.example.weatherapp.model.WeatherData
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherDataTest {

    @Test
    fun testWeatherDataSerialization() {
        val current = CurrentWeather(
            datetime = "2024-05-10T12:00:00",
            temperature = 25.0,
            feelsLike = 24.0,
            humidity = 50.0,
            windSpeed = 10.0,
            windDirection = 180.0,
            conditions = "Clear",
            icon = "clear-day"
        )
        val forecast = listOf(
            DailyForecast("2024-05-11", 28.0, 15.0, "Sunny", "clear-day", 40.0, 12.0),
            DailyForecast("2024-05-12", 27.0, 16.0, "Cloudy", "cloudy", 45.0, 10.0)
        )
        val weatherData = WeatherData(30.0, 31.0, current, forecast)

        val serialized = TestUtils.serialize(weatherData)
        val deserialized = TestUtils.deserialize<WeatherData>(serialized)

        assertEquals(weatherData, deserialized)
    }
}