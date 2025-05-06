package com.example.weatherapp.network

import android.content.Context
import android.util.Log
import com.example.weatherapp.WeatherApplication
import com.example.weatherapp.model.CurrentWeather
import com.example.weatherapp.model.DailyForecast
import com.example.weatherapp.model.WeatherData
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class WeatherApiHelper(private val context: Context) {

    companion object {
        private const val API_KEY = "ESB4PCSMMMRZP2UZ37DQEMYXC"
        private const val BASE_URL = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/"
    }

    fun fetchWeatherData(latitude: Double, longitude: Double, callback: (Result<WeatherData>) -> Unit) {
        val urlString = "${BASE_URL}${latitude},${longitude}?unitGroup=metric&include=current,hours,daily&key=${API_KEY}&contentType=json"

        Thread {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    Log.d("WeatherApiHelper", "API response: $response")

                    val weatherData = parseWeatherResponse(response.toString())
                    (context.applicationContext as WeatherApplication).putCache("lastWeatherData", weatherData)
                    callback(Result.success(weatherData))
                } else {
                    callback(Result.failure(Exception("Failed to fetch data: HTTP $responseCode")))
                }
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }.start()
    }

    private fun parseWeatherResponse(jsonString: String): WeatherData {
        val jsonObject = org.json.JSONObject(jsonString)
        val latitude = jsonObject.getDouble("latitude")
        val longitude = jsonObject.getDouble("longitude")

        val current = jsonObject.getJSONObject("currentConditions")
        val currentWeather = CurrentWeather(
            datetime = current.getString("datetime"),
            temperature = current.getDouble("temp"),
            feelsLike = current.getDouble("feelslike"),
            humidity = current.getDouble("humidity"),
            windSpeed = current.getDouble("windspeed"),
            windDirection = current.getDouble("winddir"),
            conditions = current.getString("conditions"),
            icon = current.getString("icon")
        )

        val daysArray = jsonObject.getJSONArray("days")
        val forecast = mutableListOf<DailyForecast>()
        for (i in 0 until daysArray.length()) {
            val day = daysArray.getJSONObject(i)
            forecast.add(DailyForecast(
                datetime = day.getString("datetime"),
                tempMax = day.getDouble("tempmax"),
                tempMin = day.getDouble("tempmin"),
                conditions = day.getString("conditions"),
                icon = day.getString("icon"),
                humidity = day.getDouble("humidity"),
                windSpeed = day.getDouble("windspeed")
            ))
        }

        return WeatherData(latitude, longitude, currentWeather, forecast)
    }
}
