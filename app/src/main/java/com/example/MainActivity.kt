package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.navigation.NavGraph
import com.example.ui.screens.exams.ExamsViewModel
import com.example.ui.screens.focus.FocusViewModel
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.progress.ProgressViewModel
import com.example.ui.screens.schedule.ScheduleViewModel
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.screens.subjects.SubjectsViewModel
import com.example.ui.theme.StudyFlowTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Notification permission granted or denied
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as StudyFlowApplication

        val homeViewModel = HomeViewModel(app.studyRepository, app.userPreferencesRepository)
        val scheduleViewModel = ScheduleViewModel(app.studyRepository)
        val subjectsViewModel = SubjectsViewModel(app.studyRepository, app)
        val focusViewModel = FocusViewModel(app.pomodoroTimerManager, app.studyRepository, app.userPreferencesRepository)
        val progressViewModel = ProgressViewModel(app.studyRepository, app.userPreferencesRepository)
        val examsViewModel = ExamsViewModel(app.studyRepository)
        val settingsViewModel = SettingsViewModel(app.userPreferencesRepository, app.studyRepository)

        // Request POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val settings by app.userPreferencesRepository.appSettingsFlow.collectAsStateWithLifecycle(
                initialValue = com.example.data.preferences.AppSettings()
            )

            StudyFlowTheme(
                themeMode = settings.themeMode,
                accentColor = settings.accentColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavGraph(
                        homeViewModel = homeViewModel,
                        scheduleViewModel = scheduleViewModel,
                        subjectsViewModel = subjectsViewModel,
                        focusViewModel = focusViewModel,
                        progressViewModel = progressViewModel,
                        examsViewModel = examsViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}
