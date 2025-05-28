package com.example.taqs360.home.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.example.taqs360.home.model.pojo.Forecast

class TemperatureGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var forecasts: List<Forecast> = emptyList()
    private val graphDrawer = GraphDrawer()

    fun setForecasts(newForecasts: List<Forecast>) {
        forecasts = newForecasts
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (forecasts.size < 2) return

        val width = width.toFloat()
        val height = height.toFloat()
        val padding = 16f

        val temperatures = forecasts.map { it.main.temp }
        val minTemp = temperatures.minOrNull() ?: 0f
        val maxTemp = temperatures.maxOrNull() ?: 0f
        val tempRange = if (maxTemp == minTemp) 10f else maxTemp - minTemp

        val xCoords = mutableListOf<Float>()
        val yCoords = mutableListOf<Float>()
        forecasts.forEachIndexed { index, forecast ->
            val x = padding + (index * (width - 2 * padding) / (forecasts.size - 1))
            val y = height - padding - ((forecast.main.temp - minTemp) / tempRange * (height - 2 * padding))
            xCoords.add(x)
            yCoords.add(y)
        }

        graphDrawer.drawTemperatureGraph(canvas, temperatures, xCoords, yCoords, minTemp, maxTemp)
    }
}