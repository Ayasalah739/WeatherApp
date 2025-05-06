package com.example.weatherapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ActivityForecastBinding
import com.example.weatherapp.databinding.ItemForecastBinding
import com.example.weatherapp.model.DailyForecast
import java.text.SimpleDateFormat
import java.util.*

class ForecastActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForecastBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForecastBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val forecastData = intent.getSerializableExtra("forecastData") as? ArrayList<DailyForecast>

        if (forecastData != null) {
            binding.forecastRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.forecastRecyclerView.adapter = ForecastAdapter(forecastData)
        } else {
            finish()
            Toast.makeText(this, "No forecast data available", Toast.LENGTH_SHORT).show()
        }
    }

    private inner class ForecastAdapter(private val forecastList: List<DailyForecast>) :
        RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
            val itemBinding = ItemForecastBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ForecastViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
            holder.bind(forecastList[position])
        }

        override fun getItemCount(): Int = forecastList.size

        inner class ForecastViewHolder(private val binding: ItemForecastBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(forecast: DailyForecast) {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = inputFormat.parse(forecast.datetime)
                val outputFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                binding.dateText.text = outputFormat.format(date ?: forecast.datetime)

                binding.tempText.text = "H: ${forecast.tempMax}° L: ${forecast.tempMin}°"
                binding.conditionsText.text = forecast.conditions
                binding.humidityText.text = "Humidity: ${forecast.humidity}%"
                binding.windText.text = "Wind: ${forecast.windSpeed} km/h"

                // Set weather icon
                val iconResId = when (forecast.icon) {
                    "clear-day" -> R.drawable.sun
                    "clear-night" -> R.drawable.clear_night
                    "rain" -> R.drawable.rain
                    "snow" -> R.drawable.snow
                    "sleet" -> R.drawable.sleet
                    "wind" -> R.drawable.wind
                    "fog" -> R.drawable.fog
                    "cloudy" -> R.drawable.cloud
                    "partly-cloudy-day" -> R.drawable.partly_day
                    "partly-cloudy-night" -> R.drawable.partly_night
                    else -> R.drawable.sun
                }
                binding.weatherIcon.setImageResource(iconResId)
            }
        }
    }
}