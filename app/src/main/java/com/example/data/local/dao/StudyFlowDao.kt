package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.ChapterEntity
import com.example.data.local.entities.ExamChapterEntity
import com.example.data.local.entities.ExamEntity
import com.example.data.local.entities.PomodoroSessionEntity
import com.example.data.local.entities.StudySessionEntity
import com.example.data.local.entities.SubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyFlowDao {

    // --- Subjects ---
    @Query("SELECT * FROM subjects WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllActiveSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    fun getSubjectById(id: Long): Flow<SubjectEntity?>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectByIdSync(id: Long): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    // --- Chapters ---
    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY id ASC")
    fun getChaptersForSubject(subjectId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getChapterByIdSync(id: Long): ChapterEntity?

    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId")
    suspend fun getChaptersForSubjectSync(subjectId: Long): List<ChapterEntity>

    @Query("SELECT * FROM chapters ORDER BY createdAt DESC")
    fun getAllChapters(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getChapterById(id: Long): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity)

    // --- Study Sessions ---
    @Query("SELECT * FROM study_sessions WHERE dateEpochDay = :epochDay ORDER BY startTimeMinuteOfDay ASC")
    fun getSessionsForDay(epochDay: Long): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE dateEpochDay >= :startEpochDay AND dateEpochDay <= :endEpochDay ORDER BY dateEpochDay ASC, startTimeMinuteOfDay ASC")
    fun getSessionsForDateRange(startEpochDay: Long, endEpochDay: Long): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions ORDER BY dateEpochDay DESC, startTimeMinuteOfDay DESC")
    fun getAllStudySessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE id = :id")
    suspend fun getStudySessionById(id: Long): StudySessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(session: StudySessionEntity): Long

    @Update
    suspend fun updateStudySession(session: StudySessionEntity)

    @Delete
    suspend fun deleteStudySession(session: StudySessionEntity)

    // --- Pomodoro Sessions ---
    @Query("SELECT * FROM pomodoro_sessions ORDER BY timestamp DESC")
    fun getAllPomodoroSessions(): Flow<List<PomodoroSessionEntity>>

    @Query("SELECT * FROM pomodoro_sessions WHERE timestamp >= :startMs ORDER BY timestamp DESC")
    fun getPomodoroSessionsSince(startMs: Long): Flow<List<PomodoroSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPomodoroSession(session: PomodoroSessionEntity): Long

    // --- Exams ---
    @Query("SELECT * FROM exams ORDER BY examDateEpochDay ASC, examTimeMinuteOfDay ASC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE id = :id")
    fun getExamById(id: Long): Flow<ExamEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity): Long

    @Update
    suspend fun updateExam(exam: ExamEntity)

    @Delete
    suspend fun deleteExam(exam: ExamEntity)

    // --- Exam Chapters ---
    @Query("SELECT chapterId FROM exam_chapters WHERE examId = :examId")
    fun getChapterIdsForExam(examId: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamChapter(examChapter: ExamChapterEntity)

    @Query("DELETE FROM exam_chapters WHERE examId = :examId")
    suspend fun clearChaptersForExam(examId: Long)

    // Clear all tables for Reset Data
    @Query("DELETE FROM subjects")
    suspend fun deleteAllSubjects()

    @Query("DELETE FROM chapters")
    suspend fun deleteAllChapters()

    @Query("DELETE FROM study_sessions")
    suspend fun deleteAllStudySessions()

    @Query("DELETE FROM pomodoro_sessions")
    suspend fun deleteAllPomodoroSessions()

    @Query("DELETE FROM exams")
    suspend fun deleteAllExams()
}
