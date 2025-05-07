package com.example.weatherapp

import com.example.weatherapp.model.CurrentWeather
import com.example.weatherapp.model.DailyForecast
import com.example.weatherapp.model.WeatherData
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherDataTest {

    @Test
    fun testWeatherDataSerialization() {
        // Creating a sample current weather object
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

        // Creating a list of daily forecasts for two days
        val forecast = listOf(
            DailyForecast(
                datetime = "2024-05-11",
                tempMax = 28.0,
                tempMin = 15.0,
                temp = 21.0,
                conditions = "Sunny",
                icon = "clear-day",
                humidity = 40.0,
                windSpeed = 12.0
            ),
            DailyForecast(
                datetime = "2024-05-12",
                tempMax = 27.0,
                tempMin = 16.0,
                temp = 21.5,
                conditions = "Cloudy",
                icon = "cloudy",
                humidity = 45.0,
                windSpeed = 10.0
            )
        )

        // Wrapping everything into a WeatherData object
        val weatherData = WeatherData(30.0, 31.0, current, forecast)

        // Serializing the WeatherData object to a JSON/string format
        val serialized = TestUtils.serialize(weatherData)

        // Deserializing back to WeatherData object
        val deserialized = TestUtils.deserialize<WeatherData>(serialized)

        // Making sure that the object before and after serialization are the same
        assertEquals(weatherData, deserialized)
    }
}
