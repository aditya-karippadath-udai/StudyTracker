package com.example.data.repository

import com.example.data.local.dao.StudyFlowDao
import com.example.data.local.entities.ChapterEntity
import com.example.data.local.entities.ExamChapterEntity
import com.example.data.local.entities.ExamEntity
import com.example.data.local.entities.PomodoroSessionEntity
import com.example.data.local.entities.StudySessionEntity
import com.example.data.local.entities.SubjectEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class StudyRepository(private val dao: StudyFlowDao) {

    val allSubjects: Flow<List<SubjectEntity>> = dao.getAllSubjects()
    val activeSubjects: Flow<List<SubjectEntity>> = dao.getAllActiveSubjects()
    val allChapters: Flow<List<ChapterEntity>> = dao.getAllChapters()
    val allStudySessions: Flow<List<StudySessionEntity>> = dao.getAllStudySessions()
    val allPomodoroSessions: Flow<List<PomodoroSessionEntity>> = dao.getAllPomodoroSessions()
    val allExams: Flow<List<ExamEntity>> = dao.getAllExams()

    fun getSubjectById(id: Long): Flow<SubjectEntity?> = dao.getSubjectById(id)
    suspend fun getSubjectByIdSync(id: Long): SubjectEntity? = dao.getSubjectByIdSync(id)
    suspend fun getChapterByIdSync(id: Long): ChapterEntity? = dao.getChapterByIdSync(id)

    fun getChaptersForSubject(subjectId: Long): Flow<List<ChapterEntity>> = dao.getChaptersForSubject(subjectId)
    suspend fun getChaptersForSubjectSync(subjectId: Long): List<ChapterEntity> = dao.getChaptersForSubjectSync(subjectId)

    fun getSessionsForDay(epochDay: Long): Flow<List<StudySessionEntity>> = dao.getSessionsForDay(epochDay)
    fun getSessionsForDateRange(startEpochDay: Long, endEpochDay: Long): Flow<List<StudySessionEntity>> = dao.getSessionsForDateRange(startEpochDay, endEpochDay)

    fun getChapterIdsForExam(examId: Long): Flow<List<Long>> = dao.getChapterIdsForExam(examId)

    // --- Subject CRUD ---
    suspend fun insertSubject(subject: SubjectEntity): Long = dao.insertSubject(subject)
    suspend fun updateSubject(subject: SubjectEntity) = dao.updateSubject(subject)
    suspend fun deleteSubject(subject: SubjectEntity) = dao.deleteSubject(subject)

    // --- Chapter CRUD ---
    suspend fun insertChapter(chapter: ChapterEntity): Long = dao.insertChapter(chapter)
    suspend fun updateChapter(chapter: ChapterEntity) = dao.updateChapter(chapter)
    suspend fun deleteChapter(chapter: ChapterEntity) = dao.deleteChapter(chapter)

    // --- Study Session CRUD ---
    suspend fun insertStudySession(session: StudySessionEntity): Long = dao.insertStudySession(session)
    suspend fun updateStudySession(session: StudySessionEntity) = dao.updateStudySession(session)
    suspend fun deleteStudySession(session: StudySessionEntity) = dao.deleteStudySession(session)

    // --- Pomodoro CRUD ---
    suspend fun insertPomodoroSession(session: PomodoroSessionEntity): Long = dao.insertPomodoroSession(session)

    // --- Exam CRUD ---
    suspend fun insertExam(exam: ExamEntity, chapterIds: List<Long> = emptyList()): Long {
        val examId = dao.insertExam(exam)
        dao.clearChaptersForExam(examId)
        chapterIds.forEach { chapterId ->
            dao.insertExamChapter(ExamChapterEntity(examId = examId, chapterId = chapterId))
        }
        return examId
    }

    suspend fun updateExam(exam: ExamEntity, chapterIds: List<Long>) {
        dao.updateExam(exam)
        dao.clearChaptersForExam(exam.id)
        chapterIds.forEach { chapterId ->
            dao.insertExamChapter(ExamChapterEntity(examId = exam.id, chapterId = chapterId))
        }
    }

    suspend fun deleteExam(exam: ExamEntity) = dao.deleteExam(exam)

    suspend fun resetAllData() {
        dao.deleteAllSubjects()
        dao.deleteAllChapters()
        dao.deleteAllStudySessions()
        dao.deleteAllPomodoroSessions()
        dao.deleteAllExams()
    }

    // Seed default sample data if subjects list is empty
    suspend fun seedInitialDataIfEmpty() {
        // Will check if subjects list is empty and pre-populate initial subject & chapters
        val existing = dao.getSubjectByIdSync(1)
        if (existing == null) {
            val mathId = dao.insertSubject(
                SubjectEntity(
                    name = "Mathematics",
                    description = "Calculus, Linear Algebra & Geometry",
                    colorHex = "#2196F3",
                    iconName = "Calculate",
                    targetHoursPerWeek = 6.0f
                )
            )
            val physicsId = dao.insertSubject(
                SubjectEntity(
                    name = "Physics",
                    description = "Electromagnetism & Quantum Mechanics",
                    colorHex = "#8B5CF6",
                    iconName = "Science",
                    targetHoursPerWeek = 5.0f
                )
            )
            val csId = dao.insertSubject(
                SubjectEntity(
                    name = "Computer Science",
                    description = "Data Structures, Algorithms & OS",
                    colorHex = "#22C55E",
                    iconName = "Code",
                    targetHoursPerWeek = 8.0f
                )
            )

            // Seed Chapters for Math
            dao.insertChapter(ChapterEntity(subjectId = mathId, name = "Limits", isCompleted = true, priority = "Medium"))
            dao.insertChapter(ChapterEntity(subjectId = mathId, name = "Derivatives", isCompleted = true, priority = "High"))
            val c3 = dao.insertChapter(ChapterEntity(subjectId = mathId, name = "Applications of Derivatives", isCompleted = true, priority = "High"))
            val c4 = dao.insertChapter(ChapterEntity(subjectId = mathId, name = "Integration", isCompleted = false, priority = "Critical"))
            val c5 = dao.insertChapter(ChapterEntity(subjectId = mathId, name = "Differential Equations", isCompleted = false, priority = "Medium"))

            // Seed Chapters for Physics
            dao.insertChapter(ChapterEntity(subjectId = physicsId, name = "Electrostatics", isCompleted = true, priority = "Medium"))
            val pc2 = dao.insertChapter(ChapterEntity(subjectId = physicsId, name = "Electromagnetism", isCompleted = false, priority = "High"))
            dao.insertChapter(ChapterEntity(subjectId = physicsId, name = "Wave Optics", isCompleted = false, priority = "Low"))

            // Seed Chapters for CS
            dao.insertChapter(ChapterEntity(subjectId = csId, name = "Arrays & Strings", isCompleted = true, priority = "Low"))
            dao.insertChapter(ChapterEntity(subjectId = csId, name = "Trees & Graphs", isCompleted = true, priority = "High"))
            val csc3 = dao.insertChapter(ChapterEntity(subjectId = csId, name = "Operating Systems", isCompleted = false, priority = "Critical"))

            // Seed Today's Study Sessions
            val today = LocalDate.now().toEpochDay()
            dao.insertStudySession(
                StudySessionEntity(
                    subjectId = mathId,
                    chapterId = c4,
                    title = "Math Integration Practice",
                    dateEpochDay = today,
                    startTimeMinuteOfDay = 600, // 10:00 AM
                    endTimeMinuteOfDay = 645,   // 10:45 AM
                    durationMinutes = 45,
                    priority = "Critical",
                    isCompleted = true,
                    completedAt = System.currentTimeMillis() - 3600_000
                )
            )
            dao.insertStudySession(
                StudySessionEntity(
                    subjectId = physicsId,
                    chapterId = pc2,
                    title = "Electromagnetism Problem Solving",
                    dateEpochDay = today,
                    startTimeMinuteOfDay = 660, // 11:00 AM
                    endTimeMinuteOfDay = 690,   // 11:30 AM
                    durationMinutes = 30,
                    priority = "High",
                    isCompleted = false
                )
            )
            dao.insertStudySession(
                StudySessionEntity(
                    subjectId = csId,
                    chapterId = csc3,
                    title = "OS Process Management & Threads",
                    dateEpochDay = today,
                    startTimeMinuteOfDay = 1020, // 5:00 PM
                    endTimeMinuteOfDay = 1080,  // 6:00 PM
                    durationMinutes = 60,
                    priority = "High",
                    isCompleted = false
                )
            )

            // Seed upcoming Exam
            val examDate = LocalDate.now().plusDays(12).toEpochDay()
            val examId = dao.insertExam(
                ExamEntity(
                    subjectId = mathId,
                    name = "Mathematics Final Exam",
                    examDateEpochDay = examDate,
                    examTimeMinuteOfDay = 600,
                    location = "Hall 4B",
                    notes = "Covers Integration & Differential Equations"
                )
            )
            dao.insertExamChapter(ExamChapterEntity(examId = examId, chapterId = c3))
            dao.insertExamChapter(ExamChapterEntity(examId = examId, chapterId = c4))
            dao.insertExamChapter(ExamChapterEntity(examId = examId, chapterId = c5))

            // Seed some Pomodoro sessions for streak
            val nowMs = System.currentTimeMillis()
            dao.insertPomodoroSession(PomodoroSessionEntity(subjectId = mathId, durationMinutes = 25, timestamp = nowMs - 86400000 * 2))
            dao.insertPomodoroSession(PomodoroSessionEntity(subjectId = physicsId, durationMinutes = 25, timestamp = nowMs - 86400000))
            dao.insertPomodoroSession(PomodoroSessionEntity(subjectId = mathId, durationMinutes = 25, timestamp = nowMs))
        }
    }
}
