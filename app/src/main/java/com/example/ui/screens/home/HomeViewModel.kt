package com.example.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.ExamEntity
import com.example.data.local.entities.StudySessionEntity
import com.example.data.local.entities.SubjectEntity
import com.example.data.preferences.AppSettings
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val settings: AppSettings = AppSettings(),
    val todaySessions: List<StudySessionEntity> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList(),
    val upcomingExams: List<ExamEntity> = emptyList(),
    val todayStudiedMinutes: Int = 0,
    val todayCompletedTasks: Int = 0,
    val todayTotalTasks: Int = 0,
    val currentStreak: Int = 7,
    val weeklyProgressPercent: Int = 0,
    val weeklyCompletedSessions: Int = 0,
    val weeklyTotalSessions: Int = 0
)

class HomeViewModel(
    private val repository: StudyRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val todayEpochDay = LocalDate.now().toEpochDay()
    private val startOfWeekEpochDay = LocalDate.now().minusDays(LocalDate.now().dayOfWeek.value.toLong() - 1).toEpochDay()
    private val endOfWeekEpochDay = startOfWeekEpochDay + 6

    private val weekDataFlow = combine(
        repository.getSessionsForDateRange(startOfWeekEpochDay, endOfWeekEpochDay),
        repository.allPomodoroSessions
    ) { weekSessions, pomodoros -> Pair(weekSessions, pomodoros) }

    val uiState: StateFlow<HomeUiState> = combine(
        preferencesRepository.appSettingsFlow,
        repository.getSessionsForDay(todayEpochDay),
        repository.activeSubjects,
        repository.allExams,
        weekDataFlow
    ) { settings, todaySessions, subjects, exams, weekData ->
        val weekSessions = weekData.first
        val pomodoros = weekData.second

        val todayCompleted = todaySessions.filter { it.isCompleted }
        val todayStudiedMins = todayCompleted.sumOf { it.durationMinutes } +
                pomodoros.filter { LocalDate.ofEpochDay(it.timestamp / 86400000L) == LocalDate.now() }.sumOf { it.durationMinutes }

        val weekCompletedCount = weekSessions.count { it.isCompleted }
        val weekTotalCount = weekSessions.size
        val weekPercent = if (weekTotalCount > 0) (weekCompletedCount * 100) / weekTotalCount else 0

        // Calculate streak
        val datesWithActivity = (weekSessions.filter { it.isCompleted }.map { it.dateEpochDay } +
                pomodoros.map { it.timestamp / 86400000L }).toSet().sortedDescending()

        var streak = 0
        var checkDate = todayEpochDay
        while (datesWithActivity.contains(checkDate) || datesWithActivity.contains(checkDate - 1)) {
            if (datesWithActivity.contains(checkDate)) {
                streak++
                checkDate--
            } else if (checkDate == todayEpochDay) {
                // Today has no activity yet, but yesterday did
                checkDate--
            } else {
                break
            }
        }
        if (streak == 0 && datesWithActivity.contains(todayEpochDay)) streak = 1

        HomeUiState(
            settings = settings,
            todaySessions = todaySessions,
            subjects = subjects,
            upcomingExams = exams.filter { !it.isCompleted && it.examDateEpochDay >= todayEpochDay }.take(3),
            todayStudiedMinutes = todayStudiedMins,
            todayCompletedTasks = todayCompleted.size,
            todayTotalTasks = todaySessions.size,
            currentStreak = maxOf(1, streak),
            weeklyProgressPercent = weekPercent,
            weeklyCompletedSessions = weekCompletedCount,
            weeklyTotalSessions = weekTotalCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun markSessionCompleted(session: StudySessionEntity, completed: Boolean) {
        viewModelScope.launch {
            repository.updateStudySession(
                session.copy(
                    isCompleted = completed,
                    completedAt = if (completed) System.currentTimeMillis() else null
                )
            )
        }
    }
}
