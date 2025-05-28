package com.example.taqs360.home.util;

public class WeatherUnitConverter {
    static {
        System.loadLibrary("taqs360");
    }

    public native float convertTemperature(float temp, String fromUnit, String toUnit);
    public native float convertWindSpeed(float speed, String fromUnit, String toUnit);
}