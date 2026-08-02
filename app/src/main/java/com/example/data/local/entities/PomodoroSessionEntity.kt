package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long? = null,
    val chapterId: Long? = null,
    val studySessionId: Long? = null,
    val durationMinutes: Int = 25,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "Focus" // Focus, ShortBreak, LongBreak
)
