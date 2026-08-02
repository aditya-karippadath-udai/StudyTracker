package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.R

object NotificationHelper {

    const val CHANNEL_STUDY_REMINDERS = "study_reminders"
    const val CHANNEL_EXAM_REMINDERS = "exam_reminders"
    const val CHANNEL_POMODORO = "pomodoro"
    const val CHANNEL_DAILY_REMINDERS = "daily_reminders"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val studyChannel = NotificationChannel(
                CHANNEL_STUDY_REMINDERS,
                "Study Session Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for scheduled study sessions"
            }

            val examChannel = NotificationChannel(
                CHANNEL_EXAM_REMINDERS,
                "Exam Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for upcoming exams"
            }

            val pomodoroChannel = NotificationChannel(
                CHANNEL_POMODORO,
                "Pomodoro Focus Timer",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Pomodoro timer completion notifications"
            }

            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY_REMINDERS,
                "Daily Study Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily study encouragement notifications"
            }

            manager.createNotificationChannel(studyChannel)
            manager.createNotificationChannel(examChannel)
            manager.createNotificationChannel(pomodoroChannel)
            manager.createNotificationChannel(dailyChannel)
        }
    }

    fun showNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        message: String
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        manager.notify(notificationId, builder.build())
    }
}
