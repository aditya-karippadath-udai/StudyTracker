package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.StudyFlowDao
import com.example.data.local.entities.ChapterEntity
import com.example.data.local.entities.ExamChapterEntity
import com.example.data.local.entities.ExamEntity
import com.example.data.local.entities.PomodoroSessionEntity
import com.example.data.local.entities.StudySessionEntity
import com.example.data.local.entities.SubjectEntity

@Database(
    entities = [
        SubjectEntity::class,
        ChapterEntity::class,
        StudySessionEntity::class,
        PomodoroSessionEntity::class,
        ExamEntity::class,
        ExamChapterEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studyFlowDao(): StudyFlowDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_flow_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
