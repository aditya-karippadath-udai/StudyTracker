package com.example.ui.screens.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.ChapterEntity
import com.example.data.local.entities.SubjectEntity
import com.example.data.repository.StudyRepository
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

class SubjectsViewModel(private val repository: StudyRepository) : ViewModel() {

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

    fun addChapter(subjectId: Long, name: String, description: String, priority: String, estimatedMins: Int, notes: String) {
        viewModelScope.launch {
            repository.insertChapter(
                ChapterEntity(
                    subjectId = subjectId,
                    name = name,
                    description = description,
                    priority = priority,
                    estimatedMinutes = estimatedMins,
                    notes = notes
                )
            )
        }
    }

    fun toggleChapterCompletion(chapter: ChapterEntity) {
        viewModelScope.launch {
            repository.updateChapter(
                chapter.copy(
                    isCompleted = !chapter.isCompleted,
                    completedAt = if (!chapter.isCompleted) System.currentTimeMillis() else null
                )
            )
        }
    }

    fun deleteChapter(chapter: ChapterEntity) {
        viewModelScope.launch {
            repository.deleteChapter(chapter)
        }
    }
}
