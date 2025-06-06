package com.example.weatherapp.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.weatherapp.R
import com.example.weatherapp.WeatherApplication
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.model.CurrentWeather
import com.example.weatherapp.model.WeatherData
import com.example.weatherapp.network.WeatherApiHelper
import com.example.weatherapp.service.LocationService
import com.example.weatherapp.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var locationService: LocationService
    private lateinit var weatherApiHelper: WeatherApiHelper

    private val viewModel: WeatherViewModel by viewModels()
    private var locationTimeoutHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationService = LocationService()
        weatherApiHelper = WeatherApiHelper(this)

        setupUI()

        // Restore saved state or load cached weather data
        if (savedInstanceState != null) {
            val weatherData = savedInstanceState.getSerializable("weatherData") as? WeatherData
            viewModel.setWeatherData(weatherData)
        } else {
            val cachedData = (application as WeatherApplication).getCache("lastWeatherData") as? WeatherData
            viewModel.setWeatherData(cachedData)
        }

        // Observe weather data updates and refresh UI
        viewModel.weatherData.observe(this) { weatherData ->
            weatherData?.let { updateUI(it.current) }
        }

        checkPermissionsAndFetchWeather()
    }

    private fun setupUI() {
        // Pull-to-refresh triggers weather reload
        binding.swipeRefreshLayout.setOnRefreshListener {
            checkPermissionsAndFetchWeather()
        }

        // Button opens forecast screen with the next 5 days of data
        binding.viewForecastButton.setOnClickListener {
            val weatherData = viewModel.weatherData.value
            weatherData?.let {
                val intent = Intent(this, ForecastActivity::class.java).apply {
                    putExtra("forecastData", ArrayList(it.forecast.take(5)))
                }
                startActivity(intent)
            } ?: run {
                Toast.makeText(this, "No forecast data available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissionsAndFetchWeather() {
        // Ask for location permission if not already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            fetchWeather()
        }
    }

    private fun fetchWeather() {
        // Hide old data and show loading spinner
        binding.progressBar.visibility = View.VISIBLE
        binding.weatherContainer.visibility = View.GONE
        binding.errorView.visibility = View.GONE

        locationService.stopListening()

        // Fallback if location takes too long to get
        locationTimeoutHandler = Handler(Looper.getMainLooper())
        locationTimeoutHandler?.postDelayed({
            binding.swipeRefreshLayout.isRefreshing = false
            binding.progressBar.visibility = View.GONE
            binding.errorView.visibility = View.VISIBLE
            binding.errorView.text = "Unable to get location (timeout)"
            locationService.stopListening()
        }, 5000)

        // Start listening for GPS update
        locationService.startListening(this) { location ->
            locationTimeoutHandler?.removeCallbacksAndMessages(null)
            if (location.latitude != 0.0 && location.longitude != 0.0) {
                // Fetch weather from API when valid coordinates are received
                weatherApiHelper.fetchWeatherData(location.latitude, location.longitude) { result ->
                    runOnUiThread {
                        binding.swipeRefreshLayout.isRefreshing = false
                        binding.progressBar.visibility = View.GONE

                        result.onSuccess { weatherData ->
                            binding.weatherContainer.visibility = View.VISIBLE
                            viewModel.setWeatherData(weatherData)
                            (application as WeatherApplication).putCache("lastWeatherData", weatherData)
                        }.onFailure {
                            binding.weatherContainer.visibility = View.GONE
                            binding.errorView.visibility = View.VISIBLE
                            binding.errorView.text = if (isNetworkAvailable()) {
                                "Failed to fetch weather data"
                            } else {
                                "No internet connection"
                            }
                        }
                    }
                }
            } else {
                // Invalid location result
                runOnUiThread {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.progressBar.visibility = View.GONE
                    binding.weatherContainer.visibility = View.GONE
                    binding.errorView.visibility = View.VISIBLE
                    binding.errorView.text = "Unable to get location"
                }
            }
        }
    }

    private fun updateUI(currentWeather: CurrentWeather) {
        // Set weather values on screen
        binding.temperatureText.text = "${currentWeather.temperature}°C"
        binding.conditionsText.text = currentWeather.conditions

        binding.feelsLikeValue.text = "${currentWeather.feelsLike}°C"
        binding.humidityValue.text = "${currentWeather.humidity}%"
        binding.windValue.text = "${currentWeather.windSpeed} km/h"

        binding.feelsLikeLabel.text = "Feels like"
        binding.humidityLabel.text = "Humidity"
        binding.windLabel.text = "Wind"

        // Format and show current date
        val dateStr = currentWeather.datetime
        val date: Date? = when {
            dateStr.matches(Regex("\\d{2}:\\d{2}:\\d{2}")) -> {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse("${today}T${dateStr}")
            }
            dateStr.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")) -> {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(dateStr)
            }
            dateStr.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
            }
            else -> null
        }
        val outputFormat = SimpleDateFormat("EEE, MMM d, h:mm a", Locale.getDefault())
        binding.dateText.text = date?.let { outputFormat.format(it) } ?: dateStr

        // Show weather icon based on condition
        val iconResId = when (currentWeather.icon) {
            "clear-day" -> R.drawable.sun
            "clear-night" -> R.drawable.clear_night
            "rain" -> R.drawable.rain
            "snow" -> R.drawable.snow
            "sleet" -> R.drawable.sleet
            "wind" -> R.drawable.wind
            "fog" -> R.drawable.fog
            "cloudy" -> R.drawable.cloud
            "partly-cloudy-day" -> R.drawable.partly_day1
            "partly-cloudy-night" -> R.drawable.partly_night
            else -> R.drawable.sun
        }
        binding.weatherIcon.setImageResource(iconResId)

        // Visual temp range bar setup
        val weatherData = viewModel.weatherData.value
        val todayForecast = weatherData?.forecast?.firstOrNull()
        val minTemp = todayForecast?.tempMin ?: 0.0
        val maxTemp = todayForecast?.tempMax ?: 40.0
        val currentTemp = currentWeather.temperature

        binding.minTempText.text = "${minTemp}°"
        binding.maxTempText.text = "${maxTemp}°"

        // Position marker on the temp range bar
        val percent = ((currentTemp - minTemp) / (maxTemp - minTemp).toFloat()).coerceIn(0.0, 40.0)

        binding.tempRangeBar.post {
            val barWidth = binding.tempRangeBar.width
            val marker = binding.currentTempMarker
            val markerWidth = marker.width
            val leftMargin = (barWidth * percent - markerWidth / 2).toInt()
            val params = marker.layoutParams as FrameLayout.LayoutParams
            params.leftMargin = leftMargin
            marker.layoutParams = params
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    // Handle location permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchWeather()
            } else {
                Toast.makeText(this, "Location permission is required to fetch weather", Toast.LENGTH_SHORT).show()
                binding.swipeRefreshLayout.isRefreshing = false
                binding.progressBar.visibility = View.GONE
                binding.errorView.visibility = View.VISIBLE
                binding.errorView.text = "Location permission denied"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationService.stopListening()
        locationTimeoutHandler?.removeCallbacksAndMessages(null)
    }

    // Save current weather in case of configuration change
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.weatherData.value?.let {
            outState.putSerializable("weatherData", it)
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
