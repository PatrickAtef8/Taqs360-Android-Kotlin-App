package com.example.taqs360.home.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taqs360.R
import com.example.taqs360.home.model.pojo.Forecast
import com.example.taqs360.home.util.WeatherUnitConverter
import com.example.taqs360.home.util.WeatherUtils
import java.util.TimeZone

class ThreeHoursForecastAdapter(
    private var tempUnit: String,
    private var isArabic: Boolean = false
) : RecyclerView.Adapter<ThreeHoursForecastAdapter.ViewHolder>() {

    private var hourlyForecasts: List<Forecast> = emptyList()
    private var timeZone: TimeZone = TimeZone.getDefault()
    private var cachedUnits: String = tempUnit
    private var windSpeedUnit: String = "meters_sec"

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tv_time)
        val ivWeatherIcon: ImageView = view.findViewById(R.id.iv_weather_icon)
        val tvTemp: TextView = view.findViewById(R.id.tv_temp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_threehours_forecast, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val forecast = hourlyForecasts[position]
        val unitSymbol = when (tempUnit) {
            "metric" -> "°C"
            "imperial" -> "°F"
            "standard" -> "K"
            else -> "°C"
        }

        holder.tvTime.text = WeatherUtils.formatTime(forecast.dt, timeZone).let {
            if (isArabic) WeatherUtils.toArabicNumerals(it, true) else it
        }

        val converter = WeatherUnitConverter()
        val convertedTemp = converter.convertTemperature(forecast.main.temp, cachedUnits, tempUnit)
        val tempFormatted = WeatherUtils.formatTemperature(convertedTemp, unitSymbol)
            .let { if (isArabic) WeatherUtils.toArabicNumerals(it, true) else it }
        holder.tvTemp.text = tempFormatted

        val iconCode = forecast.weather.firstOrNull()?.icon ?: ""
        holder.ivWeatherIcon.setImageResource(
            WeatherUtils.getWeatherIconResIdFromCode(iconCode)
        )
    }

    override fun getItemCount(): Int = hourlyForecasts.size

    fun submitList(newList: List<Forecast>, timeZoneId: String, cachedUnits: String, windSpeedUnit: String) {
        this.timeZone = TimeZone.getTimeZone(timeZoneId)
        this.cachedUnits = cachedUnits
        this.windSpeedUnit = windSpeedUnit
        this.hourlyForecasts = newList.sortedBy { it.dt }
        notifyDataSetChanged()
    }

    fun updateSettings(newTempUnit: String, newIsArabic: Boolean, newWindSpeedUnit: String) {
        this.tempUnit = newTempUnit
        this.isArabic = newIsArabic
        this.windSpeedUnit = newWindSpeedUnit
        notifyDataSetChanged()
    }
}