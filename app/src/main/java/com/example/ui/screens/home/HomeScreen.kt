package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.StudySessionEntity
import com.example.ui.components.ProgressBar
import com.example.ui.components.StatCard
import com.example.ui.components.StudyFlowCard
import com.example.ui.components.parseHexColor
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToFocus: (subjectId: Long?, chapterId: Long?) -> Unit,
    onNavigateToExams: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToSubjects: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val currentHour = LocalTime.now().hour
    val greetingTime = when {
        currentHour < 12 -> "Good morning"
        currentHour < 17 -> "Good afternoon"
        else -> "Good evening"
    }

    val formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToFocus(null, null) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_start_focus")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Focus", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Header Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$greetingTime, ${uiState.settings.userName}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ready to make progress today?",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("button_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Today's Overview Grid
            item {
                Text(
                    text = "TODAY'S OVERVIEW",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val hours = uiState.todayStudiedMinutes / 60
                    val mins = uiState.todayStudiedMinutes % 60
                    val studyTimeString = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

                    StatCard(
                        title = "Studied",
                        value = studyTimeString,
                        subtitle = "Goal: ${uiState.settings.dailyStudyGoalMinutes / 60}h",
                        icon = Icons.Default.Schedule,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Tasks",
                        value = "${uiState.todayCompletedTasks} / ${uiState.todayTotalTasks}",
                        subtitle = "Completed",
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Streak",
                        value = "${uiState.currentStreak} Days",
                        subtitle = "🔥 Active",
                        icon = Icons.Default.Whatshot,
                        accentColor = Color(0xFFF97316),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Weekly Progress Card
            item {
                StudyFlowCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Weekly Progress",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${uiState.weeklyProgressPercent}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        ProgressBar(progress = uiState.weeklyProgressPercent / 100f)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${uiState.weeklyCompletedSessions} / ${uiState.weeklyTotalSessions} sessions completed this week",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Today's Schedule Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TODAY'S SCHEDULE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "View Calendar",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onNavigateToSchedule() }
                    )
                }
            }

            if (uiState.todaySessions.isEmpty()) {
                item {
                    StudyFlowCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "🎉 No sessions remaining for today",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = onNavigateToSchedule) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Study Session")
                            }
                        }
                    }
                }
            } else {
                items(uiState.todaySessions) { session ->
                    val subject = uiState.subjects.find { it.id == session.subjectId }
                    val subjectColor = parseHexColor(subject?.colorHex ?: "#2196F3")

                    val startHour = session.startTimeMinuteOfDay / 60
                    val startMin = session.startTimeMinuteOfDay % 60
                    val timeString = String.format("%02d:%02d", startHour, startMin)

                    StudyFlowCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = subjectColor.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = session.isCompleted,
                                onCheckedChange = { viewModel.markSessionCompleted(session, it) },
                                colors = CheckboxDefaults.colors(checkedColor = subjectColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = subject?.name ?: "Subject",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = subjectColor
                                )
                                Text(
                                    text = session.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$timeString • ${session.durationMinutes} min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { onNavigateToFocus(session.subjectId, session.chapterId) },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(subjectColor.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Start Focus",
                                    tint = subjectColor
                                )
                            }
                        }
                    }
                }
            }

            // Upcoming Exams Section
            if (uiState.upcomingExams.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UPCOMING EXAMS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "See All",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onNavigateToExams() }
                        )
                    }
                }

                items(uiState.upcomingExams) { exam ->
                    val subject = uiState.subjects.find { it.id == exam.subjectId }
                    val subjectColor = parseHexColor(subject?.colorHex ?: "#8B5CF6")
                    val daysLeft = maxOf(0, ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.ofEpochDay(exam.examDateEpochDay)))

                    StudyFlowCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToExams,
                        borderColor = subjectColor.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = subject?.name ?: "Subject",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = subjectColor
                                )
                                Text(
                                    text = exam.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(subjectColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$daysLeft DAYS LEFT",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = subjectColor
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
