package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val colorHex: String = "#2196F3",
    val iconName: String = "School",
    val targetHoursPerWeek: Float = 5.0f,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
