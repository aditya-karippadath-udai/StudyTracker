package com.example.domain

import com.example.data.local.entities.ChapterEntity
import com.example.data.local.entities.ExamEntity
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class StudyPlanRecommendation(
    val examName: String,
    val totalChapters: Int,
    val remainingChaptersCount: Int,
    val daysRemaining: Long,
    val chaptersPerDayRecommendation: Double,
    val dailyMinutesRecommendation: Int,
    val suggestedScheduleText: String
)

object SmartPlanner {

    fun generateRecommendation(
        exam: ExamEntity,
        examChapters: List<ChapterEntity>
    ): StudyPlanRecommendation {
        val today = LocalDate.now()
        val examDate = LocalDate.ofEpochDay(exam.examDateEpochDay)
        val daysRemaining = maxOf(1L, ChronoUnit.DAYS.between(today, examDate))

        val totalChapters = examChapters.size
        val remainingChapters = examChapters.filter { !it.isCompleted }
        val remainingCount = remainingChapters.size

        val chaptersPerDay = if (remainingCount == 0) 0.0 else (remainingCount.toDouble() / daysRemaining.toDouble())
        val totalEstimatedMins = remainingChapters.sumOf { it.estimatedMinutes }
        val dailyMinutes = if (remainingCount == 0) 0 else (totalEstimatedMins / daysRemaining).toInt().coerceAtLeast(15)

        val text = when {
            remainingCount == 0 -> "All assigned chapters completed! You are fully prepared for this exam."
            daysRemaining <= 1 -> "Exam is imminent! Focus on high-priority chapters: ${remainingChapters.take(2).joinToString { it.name }}."
            chaptersPerDay <= 0.5 -> "Light pace recommended: Complete 1 chapter every 2 days (~${dailyMinutes} mins/day)."
            chaptersPerDay <= 1.5 -> "Recommended pace: Complete 1 chapter per day (~${dailyMinutes} mins/day)."
            chaptersPerDay <= 3.0 -> "Intense pace: Study ${String.format("%.1f", chaptersPerDay)} chapters per day (~${dailyMinutes} mins/day)."
            else -> "High workload alert: ${remainingCount} chapters remaining in $daysRemaining days. Prioritize critical topics first!"
        }

        return StudyPlanRecommendation(
            examName = exam.name,
            totalChapters = totalChapters,
            remainingChaptersCount = remainingCount,
            daysRemaining = daysRemaining,
            chaptersPerDayRecommendation = chaptersPerDay,
            dailyMinutesRecommendation = dailyMinutes,
            suggestedScheduleText = text
        )
    }
}
