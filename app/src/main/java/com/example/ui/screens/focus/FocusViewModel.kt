package com.example.ui.screens.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.ChapterEntity
import com.example.data.local.entities.SubjectEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.StudyRepository
import com.example.timer.PomodoroMode
import com.example.timer.PomodoroTimerManager
import com.example.timer.PomodoroUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FocusScreenUiState(
    val timerState: PomodoroUiState = PomodoroUiState(),
    val subjects: List<SubjectEntity> = emptyList(),
    val chapters: List<ChapterEntity> = emptyList()
)

class FocusViewModel(
    val timerManager: PomodoroTimerManager,
    private val repository: StudyRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<FocusScreenUiState> = combine(
        timerManager.uiState,
        repository.activeSubjects,
        repository.allChapters,
        preferencesRepository.appSettingsFlow
    ) { timer, subjects, chapters, settings ->
        timerManager.setDurations(
            settings.pomodoroFocusDuration,
            settings.pomodoroShortBreakDuration,
            settings.pomodoroLongBreakDuration
        )
        FocusScreenUiState(
            timerState = timer,
            subjects = subjects,
            chapters = chapters
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FocusScreenUiState()
    )

    fun selectSubjectAndChapter(subjectId: Long?, chapterId: Long?) {
        viewModelScope.launch {
            val subject = if (subjectId != null) repository.getSubjectByIdSync(subjectId) else null
            val chapter = if (chapterId != null) repository.getChapterByIdSync(chapterId) else null
            timerManager.configureSession(
                subjectId = subject?.id,
                subjectName = subject?.name,
                chapterId = chapter?.id,
                chapterName = chapter?.name
            )
        }
    }

    fun selectMode(mode: PomodoroMode, durationMins: Int) {
        timerManager.selectMode(mode, durationMins)
    }

    fun startTimer() = timerManager.startTimer()
    fun pauseTimer() = timerManager.pauseTimer()
    fun resumeTimer() = timerManager.resumeTimer()
    fun resetTimer() = timerManager.resetTimer()
    fun skipSession() = timerManager.skipSession()
}
