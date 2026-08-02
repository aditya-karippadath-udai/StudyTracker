package com.example.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.ChapterEntity
import com.example.data.local.entities.SubjectEntity
import com.example.data.preferences.AppSettings
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate

data class DayStudyBar(
    val dayName: String,
    val minutesStudied: Int,
    val isToday: Boolean
)

data class SubjectProgressItem(
    val subject: SubjectEntity,
    val percent: Int,
    val completedChapters: Int,
    val totalChapters: Int
)

data class ProgressUiState(
    val settings: AppSettings = AppSettings(),
    val todayMinutes: Int = 0,
    val weeklyMinutes: Int = 0,
    val monthlyMinutes: Int = 0,
    val totalPomodoroSessions: Int = 0,
    val totalChaptersCompleted: Int = 0,
    val totalChaptersCount: Int = 0,
    val currentStreak: Int = 7,
    val longestStreak: Int = 12,
    val weeklyBarData: List<DayStudyBar> = emptyList(),
    val subjectProgressList: List<SubjectProgressItem> = emptyList(),
    val mostStudiedSubjectName: String = "Mathematics"
)

class ProgressViewModel(
    private val repository: StudyRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<ProgressUiState> = combine(
        preferencesRepository.appSettingsFlow,
        repository.allStudySessions,
        repository.allPomodoroSessions,
        repository.activeSubjects,
        repository.allChapters
    ) { settings, sessions, pomodoros, subjects, chapters ->

        val today = LocalDate.now()
        val todayEpochDay = today.toEpochDay()
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)

        val todaySessions = sessions.filter { it.dateEpochDay == todayEpochDay && it.isCompleted }
        val todayPomodoros = pomodoros.filter { LocalDate.ofEpochDay(it.timestamp / 86400000L) == today }

        val todayMins = todaySessions.sumOf { it.durationMinutes } + todayPomodoros.sumOf { it.durationMinutes }

        // Weekly bar chart data
        val weekBars = (0..6).map { i ->
            val date = startOfWeek.plusDays(i.toLong())
            val dateEpoch = date.toEpochDay()
            val dayMins = sessions.filter { it.dateEpochDay == dateEpoch && it.isCompleted }.sumOf { it.durationMinutes } +
                    pomodoros.filter { LocalDate.ofEpochDay(it.timestamp / 86400000L) == date }.sumOf { it.durationMinutes }

            val dayLabel = date.dayOfWeek.name.take(3)
            DayStudyBar(
                dayName = dayLabel,
                minutesStudied = dayMins,
                isToday = date == today
            )
        }

        val weeklyMins = weekBars.sumOf { it.minutesStudied }
        val monthlyMins = sessions.filter { it.dateEpochDay >= today.minusDays(30).toEpochDay() && it.isCompleted }.sumOf { it.durationMinutes } +
                pomodoros.filter { it.timestamp >= System.currentTimeMillis() - 30 * 86400000L }.sumOf { it.durationMinutes }

        // Subject Breakdown
        val subjectProgress = subjects.map { subject ->
            val subChapters = chapters.filter { it.subjectId == subject.id }
            val total = subChapters.size
            val completed = subChapters.count { it.isCompleted }
            val percent = if (total > 0) (completed * 100) / total else 0
            SubjectProgressItem(subject, percent, completed, total)
        }

        val mostStudied = subjectProgress.maxByOrNull { it.percent }?.subject?.name ?: "None"

        ProgressUiState(
            settings = settings,
            todayMinutes = todayMins,
            weeklyMinutes = weeklyMins,
            monthlyMinutes = monthlyMins,
            totalPomodoroSessions = pomodoros.size,
            totalChaptersCompleted = chapters.count { it.isCompleted },
            totalChaptersCount = chapters.size,
            currentStreak = 7,
            longestStreak = 12,
            weeklyBarData = weekBars,
            subjectProgressList = subjectProgress,
            mostStudiedSubjectName = mostStudied
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProgressUiState()
    )
}
