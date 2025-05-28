#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "UnitConverter"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jfloat JNICALL
Java_com_example_taqs360_home_util_WeatherUnitConverter_convertTemperature(
        JNIEnv* env,
        jobject /* this */,
        jfloat temp,
        jstring fromUnit,
        jstring toUnit) {
    const char* fromStr = env->GetStringUTFChars(fromUnit, nullptr);
    const char* toStr = env->GetStringUTFChars(toUnit, nullptr);
    std::string from(fromStr);
    std::string to(toStr);
    env->ReleaseStringUTFChars(fromUnit, fromStr);
    env->ReleaseStringUTFChars(toUnit, toStr);

    LOGD("Converting temperature: %f from %s to %s", temp, from.c_str(), to.c_str());

    if (from == to) {
        return temp;
    }

    // Convert to Kelvin as intermediate step
    float tempK;
    if (from == "metric") { // Celsius to Kelvin
        tempK = temp + 273.15f;
    } else if (from == "imperial") { // Fahrenheit to Kelvin
        tempK = (temp - 32.0f) * 5.0f / 9.0f + 273.15f;
    } else { // "standard" or Kelvin
        tempK = temp;
    }

    // Convert from Kelvin to target unit
    if (to == "metric") { // Kelvin to Celsius
        return tempK - 273.15f;
    } else if (to == "imperial") { // Kelvin to Fahrenheit
        return (tempK - 273.15f) * 9.0f / 5.0f + 32.0f;
    } else { // "standard" or Kelvin
        return tempK;
    }
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_example_taqs360_home_util_WeatherUnitConverter_convertWindSpeed(
        JNIEnv* env,
        jobject /* this */,
        jfloat speed,
        jstring fromUnit,
        jstring toUnit) {
    const char* fromStr = env->GetStringUTFChars(fromUnit, nullptr);
    const char* toStr = env->GetStringUTFChars(toUnit, nullptr);
    std::string from(fromStr);
    std::string to(toStr);
    env->ReleaseStringUTFChars(fromUnit, fromStr);
    env->ReleaseStringUTFChars(toUnit, toStr);

    // Determine source wind speed unit based on fromUnit (API units parameter)
    std::string sourceUnit = (from == "imperial") ? "miles_hour" : "meters_sec";
    LOGD("Converting wind speed: %f from %s (sourceUnit=%s) to %s", speed, from.c_str(), sourceUnit.c_str(), to.c_str());

    if (sourceUnit == to) {
        LOGD("No conversion needed, returning %f", speed);
        return speed;
    }

    // Convert between m/s and mph
    if (sourceUnit == "meters_sec" && to == "miles_hour") {
        float result = speed * 2.23694f; // m/s to mph
        LOGD("Converted %f m/s to %f mph", speed, result);
        return result;
    } else if (sourceUnit == "miles_hour" && to == "meters_sec") {
        float result = speed / 2.23694f; // mph to m/s
        LOGD("Converted %f mph to %f m/s", speed, result);
        return result;
    }

    LOGE("Invalid conversion from %s to %s, returning original speed %f", sourceUnit.c_str(), to.c_str(), speed);
    return speed; // Default case, should not occur with proper inputs
}