package com.example.taqs360.alerts.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import com.example.taqs360.R

class DismissAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra("alarm_id") ?: run {
            Log.e("DismissAlarmReceiver", "No alarm_id provided in intent")
            return
        }
        val notificationId = alarmId.hashCode()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Cancel the notification
        notificationManager.cancel(notificationId)
        Log.d("DismissAlarmReceiver", "Cancelled notification for alarm $alarmId, notificationId=$notificationId")

        // Mute alarm sound
        try {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0)
            Log.d("DismissAlarmReceiver", "Muted alarm stream for alarm $alarmId")
        } catch (e: Exception) {
            Log.e("DismissAlarmReceiver", "Error muting alarm stream: ${e.message}")
        }

        // Log the action
        if (intent.action == "com.example.taqs360.DISMISS_ALARM") {
            Log.d("DismissAlarmReceiver", "Dismiss action received for alarm $alarmId")
        } else {
            Log.w("DismissAlarmReceiver", "Unexpected action ${intent.action} for alarm $alarmId")
        }
    }
}