package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_sessions",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subjectId"]), Index(value = ["chapterId"])]
)
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val chapterId: Long? = null,
    val title: String = "",
    val dateEpochDay: Long, // LocalDate.toEpochDay()
    val startTimeMinuteOfDay: Int, // e.g. 600 = 10:00 AM
    val endTimeMinuteOfDay: Int,   // e.g. 645 = 10:45 AM
    val durationMinutes: Int = 45,
    val priority: String = "Medium",
    val notes: String = "",
    val isCompleted: Boolean = false,
    val reminderMinutesBefore: Int? = 10,
    val repeatOption: String = "None", // None, Daily, Weekly
    val completedAt: Long? = null
)
