package com.example.taqs360.home.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taqs360.R
import com.example.taqs360.home.model.pojo.Forecast
import com.example.taqs360.home.model.uidata.ForecastUiModel

class ForecastAdapter : RecyclerView.Adapter<ForecastAdapter.ViewHolder>() {
    private var forecasts: List<ForecastUiModel> = emptyList()
    private var onDayClickListener: ((Int, List<Forecast>) -> Unit)? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDay: TextView = view.findViewById(R.id.tv_day)
        val ivWeatherIcon: ImageView = view.findViewById(R.id.iv_weather_icon)
        val tvTempMin: TextView = view.findViewById(R.id.tv_temp_min)
        val tvTempMax: TextView = view.findViewById(R.id.tv_temp_max)
        val pbTempRange: ProgressBar = view.findViewById(R.id.pb_temp_range)
        val divider: View = view.findViewById(R.id.divider)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_forecast, parent, false)
        return ViewHolder(view).apply {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDayClickListener?.invoke(position, forecasts[position].forecastsForDay)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val forecast = forecasts[position]
        holder.tvDay.text = forecast.day
        holder.tvTempMin.text = "${forecast.minTemp}°"
        holder.tvTempMax.text = "${forecast.maxTemp}°"
        holder.ivWeatherIcon.setImageResource(forecast.iconResId)
        val tempRange = 40f
        val progress = ((forecast.maxTemp - forecast.minTemp) / tempRange * 100).toInt().coerceIn(0, 100)
        holder.pbTempRange.progress = progress
        holder.divider.visibility = if (position == itemCount - 1) View.GONE else View.VISIBLE
    }

    override fun getItemCount(): Int = forecasts.size.coerceAtMost(5)

    fun submitList(newList: List<ForecastUiModel>) {
        forecasts = newList.take(5)
        notifyDataSetChanged()
    }

    fun setOnDayClickListener(listener: (Int, List<Forecast>) -> Unit) {
        onDayClickListener = listener
    }
}