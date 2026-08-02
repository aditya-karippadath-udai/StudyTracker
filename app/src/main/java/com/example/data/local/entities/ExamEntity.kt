package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exams",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subjectId"])]
)
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val name: String,
    val examDateEpochDay: Long,
    val examTimeMinuteOfDay: Int = 600, // 10:00 AM
    val location: String = "",
    val notes: String = "",
    val isCompleted: Boolean = false
)
