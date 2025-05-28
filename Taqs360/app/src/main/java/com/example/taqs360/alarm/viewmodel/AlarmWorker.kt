package com.example.taqs360.alarm.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.taqs360.R
import com.example.taqs360.alarm.receiver.DismissAlarmReceiver
import com.example.taqs360.home.view.WeatherActivity

class AlarmWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        Log.d("AlarmWorker", "AlarmWorker started")
        try {
            val locationName = inputData.getString("location_name") ?: "Unknown Location"
            val alarmId = inputData.getString("alarm_id") ?: "unknown"
            val latitude = inputData.getDouble("latitude", 0.0)
            val longitude = inputData.getDouble("longitude", 0.0)
            val weatherStatus = inputData.getString("weather_status") ?: "Unknown"
            Log.d("AlarmWorker", "Processing alarm: ID=$alarmId, location=$locationName, lat=$latitude, lon=$longitude")

            val channelId = "weather_alarm_channel"
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            // Set alarm stream volume to ensure sound plays
            try {
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume , 0)
                Log.d("AlarmWorker", "Set alarm stream volume to $maxVolume")
            } catch (e: Exception) {
                Log.e("AlarmWorker", "Error setting alarm volume: ${e.message}")
            }

            // Create notification channel with sound
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val soundUri = Uri.parse("android.resource://${applicationContext.packageName}/${R.raw.alarm_sound}")
                val channel = NotificationChannel(
                    channelId,
                    "Weather Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    setSound(
                        soundUri,
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 500)
                    setBypassDnd(true)
                }
                notificationManager.createNotificationChannel(channel)
                Log.d("AlarmWorker", "Notification channel created with sound: $soundUri")
            }

            val weatherIntent = Intent(applicationContext, WeatherActivity::class.java).apply {
                putExtra("latitude", latitude)
                putExtra("longitude", longitude)
                putExtra("open_map", false)
                putExtra("alarm_id", alarmId)
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val weatherPendingIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                weatherIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val dismissIntent = Intent(applicationContext, DismissAlarmReceiver::class.java).apply {
                putExtra("alarm_id", alarmId)
                action = "com.example.taqs360.DISMISS_ALARM"
            }
            val dismissPendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                1,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val deleteIntent = Intent(applicationContext, DismissAlarmReceiver::class.java).apply {
                putExtra("alarm_id", alarmId)
                action = "com.example.taqs360.DISMISS_ALARM"
            }
            val deletePendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                2,
                deleteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationId = alarmId.hashCode()
            Log.d("AlarmWorker", "Using notificationId: $notificationId")

            val notification = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.ic_app_logo)
                .setContentTitle("Weather Alarm")
                .setContentText("$weatherStatus in $locationName")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVibrate(longArrayOf(0, 500, 500))
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(weatherPendingIntent)
                .setDeleteIntent(deletePendingIntent)
                .addAction(
                    R.drawable.ic_dismiss,
                    "Dismiss",
                    dismissPendingIntent
                )
                .build()

            notificationManager.notify(notificationId, notification)
            Log.d("AlarmWorker", "Notification sent: ID=$notificationId, alarm=$alarmId, location=$locationName")
            return Result.success()
        } catch (e: Exception) {
            Log.e("AlarmWorker", "Notification failed: ${e.message}", e)
            return Result.failure()
        }
    }
}