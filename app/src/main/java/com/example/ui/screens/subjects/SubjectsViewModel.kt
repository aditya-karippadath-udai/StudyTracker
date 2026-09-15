package com.example.ui.screens.subjects

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.ChapterEntity
import com.example.data.local.entities.SubjectEntity
import com.example.data.repository.StudyRepository
import com.example.notifications.AlarmScheduler
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class SubjectWithStats(
    val subject: SubjectEntity,
    val totalChapters: Int,
    val completedChapters: Int,
    val progressPercent: Int
)

data class SubjectsUiState(
    val subjectsWithStats: List<SubjectWithStats> = emptyList(),
    val searchQuery: String = ""
)

class SubjectsViewModel(
    private val repository: StudyRepository,
    private val context: Context? = null
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val uiState: StateFlow<SubjectsUiState> = combine(
        repository.allSubjects,
        repository.allChapters,
        _searchQuery
    ) { subjects, chapters, query ->
        val filtered = if (query.isBlank()) subjects else subjects.filter { it.name.contains(query, ignoreCase = true) }

        val withStats = filtered.map { subject ->
            val subjectChapters = chapters.filter { it.subjectId == subject.id }
            val total = subjectChapters.size
            val completed = subjectChapters.count { it.isCompleted }
            val percent = if (total > 0) (completed * 100) / total else 0
            SubjectWithStats(subject, total, completed, percent)
        }

        SubjectsUiState(subjectsWithStats = withStats, searchQuery = query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SubjectsUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun addSubject(name: String, description: String, colorHex: String, iconName: String, targetHours: Float) {
        viewModelScope.launch {
            repository.insertSubject(
                SubjectEntity(
                    name = name,
                    description = description,
                    colorHex = colorHex,
                    iconName = iconName,
                    targetHoursPerWeek = targetHours
                )
            )
        }
    }

    fun updateSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.updateSubject(subject)
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }

    // Detail Screen Chapter methods
    private val _selectedSubjectId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedSubjectChapters: StateFlow<List<ChapterEntity>> = _selectedSubjectId.flatMapLatest { id ->
        if (id != null) repository.getChaptersForSubject(id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectSubject(id: Long) {
        _selectedSubjectId.value = id
    }

    fun addChapter(
        subjectId: Long,
        name: String,
        description: String,
        priority: String,
        estimatedMins: Int,
        notes: String,
        deadlineEpochDay: Long? = null
    ) {
        viewModelScope.launch {
            val insertedId = repository.insertChapter(
                ChapterEntity(
                    subjectId = subjectId,
                    name = name,
                    description = description,
                    priority = priority,
                    estimatedMinutes = estimatedMins,
                    notes = notes,
                    deadlineEpochDay = deadlineEpochDay
                )
            )
            if (deadlineEpochDay != null && context != null) {
                scheduleTopicDeadlineNotification(context, insertedId, name, subjectId, deadlineEpochDay)
            }
        }
    }

    fun updateChapter(chapter: ChapterEntity) {
        viewModelScope.launch {
            repository.updateChapter(chapter)
            if (context != null) {
                val notificationId = (200000 + chapter.id).toInt()
                if (chapter.isCompleted || chapter.deadlineEpochDay == null) {
                    AlarmScheduler.cancelAlarm(context, notificationId)
                } else {
                    scheduleTopicDeadlineNotification(
                        context = context,
                        chapterId = chapter.id,
                        topicName = chapter.name,
                        subjectId = chapter.subjectId,
                        deadlineEpochDay = chapter.deadlineEpochDay
                    )
                }
            }
        }
    }

    fun toggleChapterCompletion(chapter: ChapterEntity) {
        viewModelScope.launch {
            val updated = chapter.copy(
                isCompleted = !chapter.isCompleted,
                completedAt = if (!chapter.isCompleted) System.currentTimeMillis() else null
            )
            repository.updateChapter(updated)
            if (context != null) {
                val notificationId = (200000 + updated.id).toInt()
                if (updated.isCompleted) {
                    AlarmScheduler.cancelAlarm(context, notificationId)
                } else if (updated.deadlineEpochDay != null) {
                    scheduleTopicDeadlineNotification(
                        context = context,
                        chapterId = updated.id,
                        topicName = updated.name,
                        subjectId = updated.subjectId,
                        deadlineEpochDay = updated.deadlineEpochDay
                    )
                }
            }
        }
    }

    fun deleteChapter(chapter: ChapterEntity) {
        viewModelScope.launch {
            repository.deleteChapter(chapter)
            if (context != null) {
                AlarmScheduler.cancelAlarm(context, (200000 + chapter.id).toInt())
            }
        }
    }

    private suspend fun scheduleTopicDeadlineNotification(
        context: Context,
        chapterId: Long,
        topicName: String,
        subjectId: Long,
        deadlineEpochDay: Long
    ) {
        val deadlineDate = LocalDate.ofEpochDay(deadlineEpochDay)
        val today = LocalDate.now()
        if (deadlineDate.isBefore(today)) return

        val subject = repository.getSubjectByIdSync(subjectId)
        val subjectName = subject?.name ?: "Subject"

        var triggerAtMillis = deadlineDate.atTime(9, 0)
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (deadlineDate == today && triggerAtMillis <= System.currentTimeMillis()) {
            triggerAtMillis = System.currentTimeMillis() + 5000
        }

        if (triggerAtMillis > System.currentTimeMillis()) {
            AlarmScheduler.scheduleExactAlarm(
                context = context,
                triggerAtMillis = triggerAtMillis,
                notificationId = (200000 + chapterId).toInt(),
                title = "📌 Topic Deadline Today!",
                message = "Deadline for topic '$topicName' in $subjectName is today!",
                channelId = NotificationHelper.CHANNEL_STUDY_REMINDERS
            )
        }
    }
}
