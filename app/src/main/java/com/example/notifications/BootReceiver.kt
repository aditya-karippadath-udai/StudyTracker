package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            NotificationHelper.createNotificationChannels(context)

            // Reschedule upcoming alarms from Room database
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val dao = db.studyFlowDao()
                val todayEpochDay = LocalDate.now().toEpochDay()

                val sessions = dao.getSessionsForDateRange(todayEpochDay, todayEpochDay + 30).first()
                for (session in sessions) {
                    if (!session.isCompleted) {
                        val sessionDate = LocalDate.ofEpochDay(session.dateEpochDay)
                        val sessionTime = LocalTime.ofSecondOfDay((session.startTimeMinuteOfDay * 60).toLong())
                        val sessionDateTime = LocalDateTime.of(sessionDate, sessionTime)

                        val remindMinutes = session.reminderMinutesBefore ?: 10
                        val triggerTime = sessionDateTime.minusMinutes(remindMinutes.toLong())
                            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                        val subject = dao.getSubjectByIdSync(session.subjectId)
                        val subjectName = subject?.name ?: "Study Session"

                        AlarmScheduler.scheduleExactAlarm(
                            context = context,
                            triggerAtMillis = triggerTime,
                            notificationId = session.id.toInt(),
                            title = "📚 Study Session Starting Soon",
                            message = "$subjectName: ${session.title}\nStarts in $remindMinutes minutes",
                            channelId = NotificationHelper.CHANNEL_STUDY_REMINDERS
                        )
                    }
                }

                // Reschedule upcoming topic deadline alarms
                val chapters = dao.getAllChapters().first()
                for (chapter in chapters) {
                    if (!chapter.isCompleted && chapter.deadlineEpochDay != null) {
                        val deadlineDate = LocalDate.ofEpochDay(chapter.deadlineEpochDay)
                        if (!deadlineDate.isBefore(LocalDate.now())) {
                            val subject = dao.getSubjectByIdSync(chapter.subjectId)
                            val subjectName = subject?.name ?: "Subject"
                            var triggerAtMillis = deadlineDate.atTime(9, 0)
                                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            if (deadlineDate == LocalDate.now() && triggerAtMillis <= System.currentTimeMillis()) {
                                triggerAtMillis = System.currentTimeMillis() + 5000
                            }
                            if (triggerAtMillis > System.currentTimeMillis()) {
                                AlarmScheduler.scheduleExactAlarm(
                                    context = context,
                                    triggerAtMillis = triggerAtMillis,
                                    notificationId = (200000 + chapter.id).toInt(),
                                    title = "📌 Topic Deadline Today!",
                                    message = "Deadline for topic '${chapter.name}' in $subjectName is today!",
                                    channelId = NotificationHelper.CHANNEL_STUDY_REMINDERS
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
