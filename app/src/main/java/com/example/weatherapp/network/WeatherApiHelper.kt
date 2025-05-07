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

    // Fetches current weather + hourly + daily forecast for given lat/lon
    fun fetchWeatherData(latitude: Double, longitude: Double, callback: (Result<WeatherData>) -> Unit) {
        val urlString = "${BASE_URL}${latitude},${longitude}?unitGroup=metric&include=current,hours,daily&key=${API_KEY}&contentType=json"

        // Run network call on background thread
        Thread {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response from input stream
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    Log.d("WeatherApiHelper", "API response: $response")

                    // Parse JSON and store result in app-level cache
                    val weatherData = parseWeatherResponse(response.toString())
                    (context.applicationContext as WeatherApplication).putCache("lastWeatherData", weatherData)

                    // Return result via callback
                    callback(Result.success(weatherData))
                } else {
                    // Handle non-200 HTTP responses
                    callback(Result.failure(Exception("Failed to fetch data: HTTP $responseCode")))
                }
            } catch (e: Exception) {
                // Handle any exceptions during request
                callback(Result.failure(e))
            }
        }.start()
    }

    // Parses the JSON response into WeatherData model
    private fun parseWeatherResponse(jsonString: String): WeatherData {
        val jsonObject = org.json.JSONObject(jsonString)
        val latitude = jsonObject.getDouble("latitude")
        val longitude = jsonObject.getDouble("longitude")

        // Parse current weather data
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

        // Parse daily forecast list
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
                temp = day.getDouble("temp"),
                windSpeed = day.getDouble("windspeed")
            ))
        }

        // Return full weather data object
        return WeatherData(latitude, longitude, currentWeather, forecast)
    }
}
