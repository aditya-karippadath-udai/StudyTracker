package com.example

import com.example.data.local.entities.ChapterEntity
import com.example.data.local.entities.ExamEntity
import com.example.domain.SmartPlanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate

class SmartPlannerTest {

    @Test
    fun `generate recommendation calculates pace correctly`() {
        val today = LocalDate.now()
        val examDate = today.plusDays(10).toEpochDay()

        val exam = ExamEntity(
            id = 1,
            subjectId = 1,
            name = "Mathematics Exam",
            examDateEpochDay = examDate
        )

        val chapters = listOf(
            ChapterEntity(id = 1, subjectId = 1, name = "Limits", isCompleted = true),
            ChapterEntity(id = 2, subjectId = 1, name = "Derivatives", isCompleted = false, estimatedMinutes = 60),
            ChapterEntity(id = 3, subjectId = 1, name = "Integration", isCompleted = false, estimatedMinutes = 60)
        )

        val rec = SmartPlanner.generateRecommendation(exam, chapters)

        assertNotNull(rec)
        assertEquals(2, rec.remainingChaptersCount)
        assertEquals(10L, rec.daysRemaining)
        assertEquals(0.2, rec.chaptersPerDayRecommendation, 0.01)
    }
}
