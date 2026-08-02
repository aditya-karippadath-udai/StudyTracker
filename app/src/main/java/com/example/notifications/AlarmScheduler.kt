package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

object AlarmScheduler {

    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_MESSAGE = "extra_message"
    const val EXTRA_CHANNEL = "extra_channel"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

    fun scheduleExactAlarm(
        context: Context,
        triggerAtMillis: Long,
        notificationId: Int,
        title: String,
        message: String,
        channelId: String
    ) {
        if (triggerAtMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_MESSAGE, message)
            putExtra(EXTRA_CHANNEL, channelId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "Security exception setting alarm: ${e.message}")
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(context: Context, notificationId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }
}
