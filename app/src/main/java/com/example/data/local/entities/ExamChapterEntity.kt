package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "exam_chapters",
    primaryKeys = ["examId", "chapterId"],
    foreignKeys = [
        ForeignKey(
            entity = ExamEntity::class,
            parentColumns = ["id"],
            childColumns = ["examId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["examId"]), Index(value = ["chapterId"])]
)
data class ExamChapterEntity(
    val examId: Long,
    val chapterId: Long
)
