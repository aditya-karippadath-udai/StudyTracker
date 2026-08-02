package com.example

import android.app.Application
import com.example.data.local.database.AppDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.StudyRepository
import com.example.notifications.NotificationHelper
import com.example.timer.PomodoroTimerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StudyFlowApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var studyRepository: StudyRepository
        private set

    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    lateinit var pomodoroTimerManager: PomodoroTimerManager
        private set

    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getDatabase(this)
        studyRepository = StudyRepository(database.studyFlowDao())
        userPreferencesRepository = UserPreferencesRepository(this)
        pomodoroTimerManager = PomodoroTimerManager(this, studyRepository)

        NotificationHelper.createNotificationChannels(this)

        // Pre-populate sample study data on initial launch
        CoroutineScope(Dispatchers.IO).launch {
            studyRepository.seedInitialDataIfEmpty()
        }
    }
}
