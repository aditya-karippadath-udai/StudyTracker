package com.example.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.ChapterEntity
import com.example.data.local.entities.StudySessionEntity
import com.example.data.local.entities.SubjectEntity
import com.example.data.repository.StudyRepository
import com.example.ui.components.DayIndicator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TopicDeadlineItem(
    val chapter: ChapterEntity,
    val subject: SubjectEntity?
)

data class ScheduleUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val daySessions: List<StudySessionEntity> = emptyList(),
    val dayTopicDeadlines: List<TopicDeadlineItem> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList(),
    val chapters: List<ChapterEntity> = emptyList(),
    val dayIndicators: Map<LocalDate, List<DayIndicator>> = emptyMap()
)

class ScheduleViewModel(private val repository: StudyRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    private val allDataFlow = combine(
        repository.allStudySessions,
        repository.allExams
    ) { allSessions, allExams -> Pair(allSessions, allExams) }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ScheduleUiState> = combine(
        _selectedDate,
        _selectedDate.flatMapLatest { repository.getSessionsForDay(it.toEpochDay()) },
        repository.activeSubjects,
        repository.allChapters,
        allDataFlow
    ) { date, daySessions, subjects, chapters, allData ->
        val allSessions = allData.first
        val allExams = allData.second

        // Calculate day indicators for calendar
        val indicatorsMap = mutableMapOf<LocalDate, MutableList<DayIndicator>>()

        allSessions.forEach { session ->
            val sessionDate = LocalDate.ofEpochDay(session.dateEpochDay)
            val list = indicatorsMap.getOrPut(sessionDate) { mutableListOf() }
            if (session.isCompleted) {
                if (!list.contains(DayIndicator.COMPLETED)) list.add(DayIndicator.COMPLETED)
            } else {
                if (!list.contains(DayIndicator.PLANNED)) list.add(DayIndicator.PLANNED)
            }
        }

        allExams.forEach { exam ->
            if (!exam.isCompleted) {
                val examDate = LocalDate.ofEpochDay(exam.examDateEpochDay)
                val list = indicatorsMap.getOrPut(examDate) { mutableListOf() }
                if (!list.contains(DayIndicator.EXAM)) list.add(DayIndicator.EXAM)
            }
        }

        chapters.forEach { chapter ->
            if (!chapter.isCompleted && chapter.deadlineEpochDay != null) {
                val deadlineDate = LocalDate.ofEpochDay(chapter.deadlineEpochDay)
                val list = indicatorsMap.getOrPut(deadlineDate) { mutableListOf() }
                if (!list.contains(DayIndicator.PLANNED)) list.add(DayIndicator.PLANNED)
            }
        }

        val dayTopicDeadlines = chapters.filter {
            it.deadlineEpochDay == date.toEpochDay()
        }.map { chapter ->
            TopicDeadlineItem(chapter, subjects.find { it.id == chapter.subjectId })
        }

        ScheduleUiState(
            selectedDate = date,
            daySessions = daySessions,
            dayTopicDeadlines = dayTopicDeadlines,
            subjects = subjects,
            chapters = chapters,
            dayIndicators = indicatorsMap
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScheduleUiState()
    )

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
    }

    fun addStudySession(
        subjectId: Long,
        chapterId: Long?,
        title: String,
        startTimeMinuteOfDay: Int,
        durationMinutes: Int,
        priority: String,
        notes: String,
        repeatOption: String
    ) {
        viewModelScope.launch {
            repository.insertStudySession(
                StudySessionEntity(
                    subjectId = subjectId,
                    chapterId = chapterId,
                    title = title,
                    dateEpochDay = _selectedDate.value.toEpochDay(),
                    startTimeMinuteOfDay = startTimeMinuteOfDay,
                    endTimeMinuteOfDay = startTimeMinuteOfDay + durationMinutes,
                    durationMinutes = durationMinutes,
                    priority = priority,
                    notes = notes,
                    repeatOption = repeatOption
                )
            )
        }
    }

    fun toggleSessionCompleted(session: StudySessionEntity) {
        viewModelScope.launch {
            repository.updateStudySession(
                session.copy(
                    isCompleted = !session.isCompleted,
                    completedAt = if (!session.isCompleted) System.currentTimeMillis() else null
                )
            )
        }
    }

    fun deleteSession(session: StudySessionEntity) {
        viewModelScope.launch {
            repository.deleteStudySession(session)
        }
    }
}
