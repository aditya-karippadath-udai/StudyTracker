package com.example.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.AppSettings
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val repository: StudyRepository
) : ViewModel() {

    val appSettings: StateFlow<AppSettings> = preferencesRepository.appSettingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    fun updateUserName(name: String) {
        viewModelScope.launch {
            preferencesRepository.updateUserName(name)
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.updateThemeMode(mode)
        }
    }

    fun updateAccentColor(color: String) {
        viewModelScope.launch {
            preferencesRepository.updateAccentColor(color)
        }
    }

    fun updateDailyGoal(minutes: Int) {
        viewModelScope.launch {
            preferencesRepository.updateDailyGoal(minutes)
        }
    }

    fun updateNotificationSettings(
        studyEnabled: Boolean,
        examEnabled: Boolean,
        dailyEnabled: Boolean
    ) {
        viewModelScope.launch {
            val current = appSettings.value
            preferencesRepository.updateNotificationSettings(
                studyEnabled = studyEnabled,
                examEnabled = examEnabled,
                dailyEnabled = dailyEnabled,
                remindBeforeMinutes = current.notificationsRemindBeforeMinutes,
                dailyTimeMinuteOfDay = current.notificationsDailyTimeMinuteOfDay
            )
        }
    }

    fun updatePomodoroSettings(
        focusMins: Int,
        shortBreakMins: Int,
        longBreakMins: Int
    ) {
        viewModelScope.launch {
            val current = appSettings.value
            preferencesRepository.updatePomodoroSettings(
                focusMinutes = focusMins,
                shortBreakMinutes = shortBreakMins,
                longBreakMinutes = longBreakMins,
                sessionsBeforeLong = current.pomodoroSessionsBeforeLongBreak,
                autoStartBreaks = current.pomodoroAutoStartBreaks,
                autoStartFocus = current.pomodoroAutoStartFocus,
                sound = current.pomodoroSound,
                vibration = current.pomodoroVibration
            )
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
            preferencesRepository.resetPreferences()
        }
    }
}
