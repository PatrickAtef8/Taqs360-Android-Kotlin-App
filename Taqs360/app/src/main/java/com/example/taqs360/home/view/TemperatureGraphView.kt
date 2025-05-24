package com.example.taqs360.home.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.taqs360.home.model.pojo.Forecast

class TemperatureGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var forecasts: List<Forecast> = emptyList()

    private val linePaint = Paint().apply {
        strokeWidth = 8f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val pointPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    fun setForecasts(newForecasts: List<Forecast>) {
        forecasts = newForecasts
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (forecasts.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()
        val padding = 16f

        val temperatures = forecasts.map { it.main.temp }
        val minTemp = temperatures.minOrNull() ?: 0f
        val maxTemp = temperatures.maxOrNull() ?: 0f
        val tempRange = if (maxTemp == minTemp) 10f else maxTemp - minTemp

        val points = forecasts.mapIndexed { index, forecast ->
            val x = padding + (index * (width - 2 * padding) / (forecasts.size - 1))
            val y = height - padding - ((forecast.main.temp - minTemp) / tempRange * (height - 2 * padding))
            Triple(x, y, forecast.main.temp)
        }

        // Draw lines with gradient colors
        for (i in 0 until points.size - 1) {
            val (x1, y1, temp1) = points[i]
            val (x2, y2, temp2) = points[i + 1]

            linePaint.color = interpolateColor(temp1, minTemp, maxTemp)
            canvas.drawLine(x1, y1, x2, y2, linePaint)
        }

        // Draw points
        points.forEach { (x, y, temp) ->
            pointPaint.color = interpolateColor(temp, minTemp, maxTemp)
            canvas.drawCircle(x, y, 12f, pointPaint)
        }
    }

    private fun interpolateColor(temp: Float, minTemp: Float, maxTemp: Float): Int {
        val ratio = (temp - minTemp) / (maxTemp - minTemp)
        return when {
            ratio < 0.5 -> blendColors(Color.GREEN, Color.YELLOW, ratio * 2)
            else -> blendColors(Color.YELLOW, Color.RED, (ratio - 0.5f) * 2)
        }
    }

    private fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio
        val r = Color.red(color1) * inverseRatio + Color.red(color2) * ratio
        val g = Color.green(color1) * inverseRatio + Color.green(color2) * ratio
        val b = Color.blue(color1) * inverseRatio + Color.blue(color2) * ratio
        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }
}
