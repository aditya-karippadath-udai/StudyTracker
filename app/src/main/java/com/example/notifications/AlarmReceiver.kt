package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(AlarmScheduler.EXTRA_TITLE) ?: "Study Reminder"
        val message = intent.getStringExtra(AlarmScheduler.EXTRA_MESSAGE) ?: "Time to study!"
        val channelId = intent.getStringExtra(AlarmScheduler.EXTRA_CHANNEL) ?: NotificationHelper.CHANNEL_STUDY_REMINDERS
        val notificationId = intent.getIntExtra(AlarmScheduler.EXTRA_NOTIFICATION_ID, 1001)

        NotificationHelper.showNotification(
            context = context,
            channelId = channelId,
            notificationId = notificationId,
            title = title,
            message = message
        )
    }
}
