package com.example.taqs360.home.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taqs360.R
import com.example.taqs360.home.model.pojo.Forecast
import com.example.taqs360.home.util.WeatherUtils
import java.util.TimeZone

class ThreeHoursForecastAdapter : RecyclerView.Adapter<ThreeHoursForecastAdapter.ViewHolder>() {
    private var hourlyForecasts: List<Forecast> = emptyList()
    private var timezoneOffset: Int = 0 // Add timezone offset

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tv_time)
        val ivWeatherIcon: ImageView = view.findViewById(R.id.iv_weather_icon)
        val tvTemp: TextView = view.findViewById(R.id.tv_temp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_threehours_forecast, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val forecast = hourlyForecasts[position]
        val timeZone = TimeZone.getTimeZone("UTC").apply {
            rawOffset = timezoneOffset * 1000 // Set timezone offset
        }
        holder.tvTime.text = WeatherUtils.formatTime(forecast.dt, timeZone)
        holder.tvTemp.text = WeatherUtils.formatTemperature(forecast.main.temp)
        holder.ivWeatherIcon.setImageResource(WeatherUtils.getWeatherIconResIdFromCode(forecast.weather[0].icon))
    }

    override fun getItemCount(): Int = hourlyForecasts.size

    fun submitList(newList: List<Forecast>, timezoneOffset: Int) {
        this.timezoneOffset = timezoneOffset
        hourlyForecasts = newList
        notifyDataSetChanged()
    }
}