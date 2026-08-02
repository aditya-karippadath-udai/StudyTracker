package com.example.timer

import android.content.Context
import com.example.data.local.entities.PomodoroSessionEntity
import com.example.data.repository.StudyRepository
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PomodoroMode(val label: String, val defaultMinutes: Int) {
    FOCUS("Focus Study", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15)
}

enum class TimerState {
    IDLE, RUNNING, PAUSED, COMPLETED
}

data class PomodoroUiState(
    val mode: PomodoroMode = PomodoroMode.FOCUS,
    val timerState: TimerState = TimerState.IDLE,
    val remainingSeconds: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val completedSessionsCount: Int = 0,
    val selectedSubjectId: Long? = null,
    val selectedSubjectName: String? = null,
    val selectedChapterId: Long? = null,
    val selectedChapterName: String? = null
)

class PomodoroTimerManager(
    private val context: Context,
    private val repository: StudyRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var targetEndTimeMs: Long = 0L

    fun configureSession(
        subjectId: Long?,
        subjectName: String?,
        chapterId: Long?,
        chapterName: String?
    ) {
        _uiState.update {
            it.copy(
                selectedSubjectId = subjectId,
                selectedSubjectName = subjectName,
                selectedChapterId = chapterId,
                selectedChapterName = chapterName
            )
        }
    }

    fun setDurations(focusMins: Int, shortBreakMins: Int, longBreakMins: Int) {
        if (_uiState.value.timerState == TimerState.IDLE) {
            val totalSecs = when (_uiState.value.mode) {
                PomodoroMode.FOCUS -> focusMins * 60
                PomodoroMode.SHORT_BREAK -> shortBreakMins * 60
                PomodoroMode.LONG_BREAK -> longBreakMins * 60
            }
            _uiState.update {
                it.copy(remainingSeconds = totalSecs, totalSeconds = totalSecs)
            }
        }
    }

    fun selectMode(mode: PomodoroMode, durationMinutes: Int) {
        timerJob?.cancel()
        val secs = durationMinutes * 60
        _uiState.update {
            it.copy(
                mode = mode,
                timerState = TimerState.IDLE,
                remainingSeconds = secs,
                totalSeconds = secs
            )
        }
    }

    fun startTimer() {
        if (_uiState.value.timerState == TimerState.RUNNING) return

        targetEndTimeMs = System.currentTimeMillis() + (_uiState.value.remainingSeconds * 1000L)
        _uiState.update { it.copy(timerState = TimerState.RUNNING) }

        timerJob?.cancel()
        timerJob = scope.launch {
            while (_uiState.value.timerState == TimerState.RUNNING) {
                val now = System.currentTimeMillis()
                val diffSecs = ((targetEndTimeMs - now) / 1000L).toInt()

                if (diffSecs <= 0) {
                    onTimerFinished()
                    break
                } else {
                    _uiState.update { it.copy(remainingSeconds = diffSecs) }
                    delay(500)
                }
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(timerState = TimerState.PAUSED) }
    }

    fun resumeTimer() {
        startTimer()
    }

    fun resetTimer() {
        timerJob?.cancel()
        val totalSecs = _uiState.value.totalSeconds
        _uiState.update {
            it.copy(
                timerState = TimerState.IDLE,
                remainingSeconds = totalSecs
            )
        }
    }

    fun skipSession() {
        timerJob?.cancel()
        val nextMode = when (_uiState.value.mode) {
            PomodoroMode.FOCUS -> PomodoroMode.SHORT_BREAK
            PomodoroMode.SHORT_BREAK -> PomodoroMode.FOCUS
            PomodoroMode.LONG_BREAK -> PomodoroMode.FOCUS
        }
        val defaultSecs = nextMode.defaultMinutes * 60
        _uiState.update {
            it.copy(
                mode = nextMode,
                timerState = TimerState.IDLE,
                remainingSeconds = defaultSecs,
                totalSeconds = defaultSecs
            )
        }
    }

    private fun onTimerFinished() {
        timerJob?.cancel()
        val currentState = _uiState.value
        val completedDurationMins = currentState.totalSeconds / 60

        _uiState.update {
            it.copy(
                timerState = TimerState.COMPLETED,
                remainingSeconds = 0,
                completedSessionsCount = if (currentState.mode == PomodoroMode.FOCUS) currentState.completedSessionsCount + 1 else currentState.completedSessionsCount
            )
        }

        // Save session if it was a FOCUS session
        if (currentState.mode == PomodoroMode.FOCUS) {
            scope.launch(Dispatchers.IO) {
                repository.insertPomodoroSession(
                    PomodoroSessionEntity(
                        subjectId = currentState.selectedSubjectId,
                        chapterId = currentState.selectedChapterId,
                        durationMinutes = completedDurationMins,
                        type = "Focus"
                    )
                )
            }
        }

        // Trigger completion notification
        NotificationHelper.showNotification(
            context = context,
            channelId = NotificationHelper.CHANNEL_POMODORO,
            notificationId = 8888,
            title = "🎉 Focus Session Complete!",
            message = "Great work! $completedDurationMins minutes completed."
        )
    }
}
