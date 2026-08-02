package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Schedule : Screen("schedule", "Schedule", Icons.Default.CalendarToday)
    object Subjects : Screen("subjects", "Subjects", Icons.Default.Book)
    object SubjectDetail : Screen("subject_detail/{subjectId}", "Subject Detail") {
        fun createRoute(subjectId: Long) = "subject_detail/$subjectId"
    }
    object Focus : Screen("focus", "Focus", Icons.Default.Timer)
    object Progress : Screen("progress", "Progress", Icons.Default.BarChart)
    object Exams : Screen("exams", "Exams")
    object Settings : Screen("settings", "Settings")
}

val BottomNavItems = listOf(
    Screen.Home,
    Screen.Schedule,
    Screen.Subjects,
    Screen.Focus,
    Screen.Progress
)
