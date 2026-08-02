package com.example.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ProgressBar
import com.example.ui.components.ProgressRing
import com.example.ui.components.StatCard
import com.example.ui.components.StudyFlowCard
import com.example.ui.components.parseHexColor

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val goalMins = uiState.settings.dailyStudyGoalMinutes
    val todayProgress = (uiState.todayMinutes.toFloat() / goalMins.toFloat()).coerceIn(0f, 1f)

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Progress & Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Streak Card
            item {
                StudyFlowCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = Color(0xFFF97316).copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Whatshot,
                                    contentDescription = null,
                                    tint = Color(0xFFF97316)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${uiState.currentStreak} DAY STREAK",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF97316)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Longest Streak: ${uiState.longestStreak} Days • Keep it up!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Day Checkmarks Row
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            uiState.weeklyBarData.forEach { day ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (day.minutesStudied > 0) Color(0xFF22C55E) else MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = day.dayName.take(1),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (day.minutesStudied > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Daily Goal Progress Ring
            item {
                StudyFlowCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Today's Study Goal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${uiState.todayMinutes / 60}h ${uiState.todayMinutes % 60}m / ${goalMins / 60}h Goal",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${(todayProgress * 100).toInt()}% completed today",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        ProgressRing(
                            progress = todayProgress,
                            size = 90.dp,
                            strokeWidth = 10.dp,
                            centerText = "${(todayProgress * 100).toInt()}%"
                        )
                    }
                }
            }

            // Weekly Study Bar Chart
            item {
                StudyFlowCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = "Weekly Study Hours",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val maxMins = maxOf(60, uiState.weeklyBarData.maxOfOrNull { it.minutesStudied } ?: 60)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            uiState.weeklyBarData.forEach { bar ->
                                val barHeightRatio = (bar.minutesStudied.toFloat() / maxMins.toFloat()).coerceIn(0.05f, 1f)

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight(barHeightRatio)
                                            .width(20.dp)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(
                                                if (bar.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = bar.dayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (bar.isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = if (bar.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Summary Stats Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Weekly Total",
                        value = "${uiState.weeklyMinutes / 60}h ${uiState.weeklyMinutes % 60}m",
                        icon = Icons.Default.Schedule,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Pomodoros",
                        value = "${uiState.totalPomodoroSessions}",
                        subtitle = "Sessions",
                        icon = Icons.Default.Timer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Subject Progress List
            item {
                Text(
                    text = "SUBJECT PROGRESS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(uiState.subjectProgressList) { item ->
                val subject = item.subject
                val color = parseHexColor(subject.colorHex)

                StudyFlowCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = color.copy(alpha = 0.4f)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${item.percent}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${item.completedChapters} / ${item.totalChapters} chapters completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ProgressBar(
                            progress = item.percent / 100f,
                            activeColor = color
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
