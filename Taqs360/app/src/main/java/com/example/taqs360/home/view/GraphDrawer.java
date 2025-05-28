package com.example.taqs360.home.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import java.util.List;

public class GraphDrawer {
    private final Paint linePaint;
    private final Paint pointPaint;

    public GraphDrawer() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(8f);
        linePaint.setStyle(Paint.Style.STROKE);

        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setStyle(Paint.Style.FILL);
    }

    public void drawTemperatureGraph(
            Canvas canvas,
            List<Float> temperatures,
            List<Float> xCoords,
            List<Float> yCoords,
            float minTemp,
            float maxTemp
    ) {
        if (temperatures.size() < 2 || temperatures.size() != xCoords.size() || xCoords.size() != yCoords.size()) {
            return;
        }

        // Draw lines
        for (int i = 0; i < temperatures.size() - 1; i++) {
            float x1 = xCoords.get(i);
            float y1 = yCoords.get(i);
            float x2 = xCoords.get(i + 1);
            float y2 = yCoords.get(i + 1);
            float temp1 = temperatures.get(i);

            // Use the average temperature of the two points for line color
            float avgTemp = (temp1 + temperatures.get(i + 1)) / 2;
            linePaint.setColor(getColorForTemperature(avgTemp));
            canvas.drawLine(x1, y1, x2, y2, linePaint);
        }

        // Draw points
        for (int i = 0; i < temperatures.size(); i++) {
            float x = xCoords.get(i);
            float y = yCoords.get(i);
            float temp = temperatures.get(i);

            pointPaint.setColor(getColorForTemperature(temp));
            canvas.drawCircle(x, y, 12f, pointPaint);
        }
    }

    private int getColorForTemperature(float temp) {
        if (temp < 15) {
            return Color.GREEN; // Cool
        } else if (temp < 25) {
            return Color.YELLOW; // Mild
        } else if (temp < 35) {
            return Color.rgb(255, 165, 0); // Orange for Warm
        } else {
            return Color.RED; // Hot
        }
    }
}