package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "study_flow_settings")

data class AppSettings(
    val userName: String = "Aditya",
    val themeMode: String = "Dark", // "System", "Dark", "Light"
    val accentColor: String = "Blue", // "Blue", "Green", "Purple"
    val dailyStudyGoalMinutes: Int = 120, // 2 hours
    val pomodoroFocusDuration: Int = 25,
    val pomodoroShortBreakDuration: Int = 5,
    val pomodoroLongBreakDuration: Int = 15,
    val pomodoroSessionsBeforeLongBreak: Int = 4,
    val pomodoroAutoStartBreaks: Boolean = false,
    val pomodoroAutoStartFocus: Boolean = false,
    val pomodoroSound: Boolean = true,
    val pomodoroVibration: Boolean = true,
    val notificationsStudyEnabled: Boolean = true,
    val notificationsExamEnabled: Boolean = true,
    val notificationsDailyEnabled: Boolean = true,
    val notificationsRemindBeforeMinutes: Int = 10,
    val notificationsDailyTimeMinuteOfDay: Int = 1140 // 7:00 PM (19 * 60)
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val DAILY_STUDY_GOAL_MINUTES = intPreferencesKey("daily_study_goal_minutes")
        
        val POMODORO_FOCUS_DURATION = intPreferencesKey("pomodoro_focus_duration")
        val POMODORO_SHORT_BREAK_DURATION = intPreferencesKey("pomodoro_short_break_duration")
        val POMODORO_LONG_BREAK_DURATION = intPreferencesKey("pomodoro_long_break_duration")
        val POMODORO_SESSIONS_BEFORE_LONG_BREAK = intPreferencesKey("pomodoro_sessions_before_long_break")
        val POMODORO_AUTO_START_BREAKS = booleanPreferencesKey("pomodoro_auto_start_breaks")
        val POMODORO_AUTO_START_FOCUS = booleanPreferencesKey("pomodoro_auto_start_focus")
        val POMODORO_SOUND = booleanPreferencesKey("pomodoro_sound")
        val POMODORO_VIBRATION = booleanPreferencesKey("pomodoro_vibration")
        
        val NOTIFICATIONS_STUDY_ENABLED = booleanPreferencesKey("notifications_study_enabled")
        val NOTIFICATIONS_EXAM_ENABLED = booleanPreferencesKey("notifications_exam_enabled")
        val NOTIFICATIONS_DAILY_ENABLED = booleanPreferencesKey("notifications_daily_enabled")
        val NOTIFICATIONS_REMIND_BEFORE_MINUTES = intPreferencesKey("notifications_remind_before_minutes")
        val NOTIFICATIONS_DAILY_TIME_MINUTE_OF_DAY = intPreferencesKey("notifications_daily_time_minute_of_day")
    }

    val appSettingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            userName = preferences[PreferencesKeys.USER_NAME] ?: "Aditya",
            themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "Dark",
            accentColor = preferences[PreferencesKeys.ACCENT_COLOR] ?: "Blue",
            dailyStudyGoalMinutes = preferences[PreferencesKeys.DAILY_STUDY_GOAL_MINUTES] ?: 120,
            pomodoroFocusDuration = preferences[PreferencesKeys.POMODORO_FOCUS_DURATION] ?: 25,
            pomodoroShortBreakDuration = preferences[PreferencesKeys.POMODORO_SHORT_BREAK_DURATION] ?: 5,
            pomodoroLongBreakDuration = preferences[PreferencesKeys.POMODORO_LONG_BREAK_DURATION] ?: 15,
            pomodoroSessionsBeforeLongBreak = preferences[PreferencesKeys.POMODORO_SESSIONS_BEFORE_LONG_BREAK] ?: 4,
            pomodoroAutoStartBreaks = preferences[PreferencesKeys.POMODORO_AUTO_START_BREAKS] ?: false,
            pomodoroAutoStartFocus = preferences[PreferencesKeys.POMODORO_AUTO_START_FOCUS] ?: false,
            pomodoroSound = preferences[PreferencesKeys.POMODORO_SOUND] ?: true,
            pomodoroVibration = preferences[PreferencesKeys.POMODORO_VIBRATION] ?: true,
            notificationsStudyEnabled = preferences[PreferencesKeys.NOTIFICATIONS_STUDY_ENABLED] ?: true,
            notificationsExamEnabled = preferences[PreferencesKeys.NOTIFICATIONS_EXAM_ENABLED] ?: true,
            notificationsDailyEnabled = preferences[PreferencesKeys.NOTIFICATIONS_DAILY_ENABLED] ?: true,
            notificationsRemindBeforeMinutes = preferences[PreferencesKeys.NOTIFICATIONS_REMIND_BEFORE_MINUTES] ?: 10,
            notificationsDailyTimeMinuteOfDay = preferences[PreferencesKeys.NOTIFICATIONS_DAILY_TIME_MINUTE_OF_DAY] ?: 1140
        )
    }

    suspend fun updateUserName(name: String) {
        context.dataStore.edit { it[PreferencesKeys.USER_NAME] = name }
    }

    suspend fun updateThemeMode(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode }
    }

    suspend fun updateAccentColor(color: String) {
        context.dataStore.edit { it[PreferencesKeys.ACCENT_COLOR] = color }
    }

    suspend fun updateDailyGoal(minutes: Int) {
        context.dataStore.edit { it[PreferencesKeys.DAILY_STUDY_GOAL_MINUTES] = minutes }
    }

    suspend fun updatePomodoroSettings(
        focusMinutes: Int,
        shortBreakMinutes: Int,
        longBreakMinutes: Int,
        sessionsBeforeLong: Int,
        autoStartBreaks: Boolean,
        autoStartFocus: Boolean,
        sound: Boolean,
        vibration: Boolean
    ) {
        context.dataStore.edit {
            it[PreferencesKeys.POMODORO_FOCUS_DURATION] = focusMinutes
            it[PreferencesKeys.POMODORO_SHORT_BREAK_DURATION] = shortBreakMinutes
            it[PreferencesKeys.POMODORO_LONG_BREAK_DURATION] = longBreakMinutes
            it[PreferencesKeys.POMODORO_SESSIONS_BEFORE_LONG_BREAK] = sessionsBeforeLong
            it[PreferencesKeys.POMODORO_AUTO_START_BREAKS] = autoStartBreaks
            it[PreferencesKeys.POMODORO_AUTO_START_FOCUS] = autoStartFocus
            it[PreferencesKeys.POMODORO_SOUND] = sound
            it[PreferencesKeys.POMODORO_VIBRATION] = vibration
        }
    }

    suspend fun updateNotificationSettings(
        studyEnabled: Boolean,
        examEnabled: Boolean,
        dailyEnabled: Boolean,
        remindBeforeMinutes: Int,
        dailyTimeMinuteOfDay: Int
    ) {
        context.dataStore.edit {
            it[PreferencesKeys.NOTIFICATIONS_STUDY_ENABLED] = studyEnabled
            it[PreferencesKeys.NOTIFICATIONS_EXAM_ENABLED] = examEnabled
            it[PreferencesKeys.NOTIFICATIONS_DAILY_ENABLED] = dailyEnabled
            it[PreferencesKeys.NOTIFICATIONS_REMIND_BEFORE_MINUTES] = remindBeforeMinutes
            it[PreferencesKeys.NOTIFICATIONS_DAILY_TIME_MINUTE_OF_DAY] = dailyTimeMinuteOfDay
        }
    }

    suspend fun resetPreferences() {
        context.dataStore.edit { it.clear() }
    }
}
