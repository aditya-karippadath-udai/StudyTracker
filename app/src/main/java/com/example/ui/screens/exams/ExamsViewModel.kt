package com.example.ui.screens.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.ChapterEntity
import com.example.data.local.entities.ExamEntity
import com.example.data.local.entities.SubjectEntity
import com.example.data.repository.StudyRepository
import com.example.domain.SmartPlanner
import com.example.domain.StudyPlanRecommendation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ExamWithRecommendation(
    val exam: ExamEntity,
    val subject: SubjectEntity?,
    val assignedChapters: List<ChapterEntity>,
    val recommendation: StudyPlanRecommendation
)

data class ExamsUiState(
    val examList: List<ExamWithRecommendation> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList(),
    val allChapters: List<ChapterEntity> = emptyList()
)

class ExamsViewModel(private val repository: StudyRepository) : ViewModel() {

    val uiState: StateFlow<ExamsUiState> = combine(
        repository.allExams,
        repository.activeSubjects,
        repository.allChapters
    ) { exams, subjects, chapters ->
        val list = exams.map { exam ->
            val subject = subjects.find { it.id == exam.subjectId }
            val assignedChaps = chapters.filter { it.subjectId == exam.subjectId }
            val recommendation = SmartPlanner.generateRecommendation(exam, assignedChaps)

            ExamWithRecommendation(
                exam = exam,
                subject = subject,
                assignedChapters = assignedChaps,
                recommendation = recommendation
            )
        }

        ExamsUiState(
            examList = list,
            subjects = subjects,
            allChapters = chapters
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExamsUiState()
    )

    fun addExam(subjectId: Long, name: String, examDateEpochDay: Long, location: String, notes: String) {
        viewModelScope.launch {
            repository.insertExam(
                ExamEntity(
                    subjectId = subjectId,
                    name = name,
                    examDateEpochDay = examDateEpochDay,
                    location = location,
                    notes = notes
                )
            )
        }
    }

    fun deleteExam(exam: ExamEntity) {
        viewModelScope.launch {
            repository.deleteExam(exam)
        }
    }
}
