package com.example.taqs360.home.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
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
        color = Color.WHITE
        strokeWidth = 8f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    private val pointPaint = Paint().apply {
        color = Color.WHITE
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

        // Find min and max temperatures
        val temperatures = forecasts.map { it.main.temp }
        val minTemp = temperatures.minOrNull() ?: 0f
        val maxTemp = temperatures.maxOrNull() ?: 0f
        val tempRange = if (maxTemp == minTemp) 10f else maxTemp - minTemp

        val points = forecasts.mapIndexed { index, forecast ->
            val x = padding + (index * (width - 2 * padding) / (forecasts.size - 1))
            val y = height - padding - ((forecast.main.temp - minTemp) / tempRange * (height - 2 * padding))
            x to y
        }

        val path = Path()
        points.forEachIndexed { index, (x, y) ->
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        canvas.drawPath(path, linePaint)

        points.forEach { (x, y) ->
            canvas.drawCircle(x, y, 12f, pointPaint)
        }
    }
}