package com.example.ui.screens.focus

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.timer.PomodoroMode
import com.example.timer.TimerState
import com.example.ui.components.ProgressRing
import com.example.ui.components.StudyFlowCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    viewModel: FocusViewModel,
    initialSubjectId: Long? = null,
    initialChapterId: Long? = null,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(initialSubjectId, initialChapterId) {
        if (initialSubjectId != null) {
            viewModel.selectSubjectAndChapter(initialSubjectId, initialChapterId)
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timer = uiState.timerState

    val mins = timer.remainingSeconds / 60
    val secs = timer.remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", mins, secs)
    val progress = if (timer.totalSeconds > 0) (timer.totalSeconds - timer.remainingSeconds).toFloat() / timer.totalSeconds else 0f

    var showSubjectSelector by remember { mutableStateOf(false) }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "FOCUS TIMER",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Mode Selector Pills
            item {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PomodoroMode.entries.forEach { mode ->
                        val isSelected = timer.mode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { viewModel.selectMode(mode, mode.defaultMinutes) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Subject / Chapter Focus Tag
            item {
                StudyFlowCard(
                    onClick = { showSubjectSelector = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = timer.selectedSubjectName?.let { sub ->
                                timer.selectedChapterName?.let { chap -> "$sub • $chap" } ?: sub
                            } ?: "Select Subject / Chapter to Focus On",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Main Timer Display Ring
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Box(contentAlignment = Alignment.Center) {
                    ProgressRing(
                        progress = progress,
                        size = 240.dp,
                        strokeWidth = 16.dp,
                        activeColor = when (timer.mode) {
                            PomodoroMode.FOCUS -> MaterialTheme.colorScheme.primary
                            PomodoroMode.SHORT_BREAK -> Color(0xFF22C55E)
                            PomodoroMode.LONG_BREAK -> Color(0xFF8B5CF6)
                        }
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = timeFormatted,
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 48.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = timer.mode.label.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Session Tracker
            item {
                Text(
                    text = "Session ${(timer.completedSessionsCount % 4) + 1} / 4",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Controls Row
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Button
                    IconButton(
                        onClick = { viewModel.resetTimer() },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("button_timer_reset")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    }

                    // Main Start/Pause Button
                    Button(
                        onClick = {
                            when (timer.timerState) {
                                TimerState.RUNNING -> viewModel.pauseTimer()
                                TimerState.PAUSED -> viewModel.resumeTimer()
                                else -> viewModel.startTimer()
                            }
                        },
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .testTag("button_timer_toggle"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (timer.timerState == TimerState.RUNNING) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (timer.timerState == TimerState.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (timer.timerState == TimerState.RUNNING) "Pause" else "Start",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Skip Button
                    IconButton(
                        onClick = { viewModel.skipSession() },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("button_timer_skip")
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Skip")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Completion Dialog
        if (timer.timerState == TimerState.COMPLETED) {
            AlertDialog(
                onDismissRequest = { viewModel.resetTimer() },
                title = { Text("🎉 Focus Session Complete!", fontWeight = FontWeight.Bold) },
                text = { Text("Great work! ${timer.totalSeconds / 60} minutes completed. Keep up the momentum!") },
                confirmButton = {
                    Button(onClick = { viewModel.resetTimer() }) {
                        Text("Continue")
                    }
                }
            )
        }

        // Select Subject/Chapter Dialog
        if (showSubjectSelector) {
            SelectFocusSubjectDialog(
                subjects = uiState.subjects,
                chapters = uiState.chapters,
                onDismiss = { showSubjectSelector = false },
                onSelect = { subId, chapId ->
                    viewModel.selectSubjectAndChapter(subId, chapId)
                    showSubjectSelector = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectFocusSubjectDialog(
    subjects: List<com.example.data.local.entities.SubjectEntity>,
    chapters: List<com.example.data.local.entities.ChapterEntity>,
    onDismiss: () -> Unit,
    onSelect: (Long?, Long?) -> Unit
) {
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()) }
    var selectedChapter by remember { mutableStateOf<com.example.data.local.entities.ChapterEntity?>(null) }

    var expandedSub by remember { mutableStateOf(false) }
    var expandedChap by remember { mutableStateOf(false) }

    val filteredChapters = chapters.filter { it.subjectId == selectedSubject?.id }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Focus Topic", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expandedSub,
                    onExpandedChange = { expandedSub = !expandedSub }
                ) {
                    OutlinedTextField(
                        value = selectedSubject?.name ?: "None",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSub) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSub,
                        onDismissRequest = { expandedSub = false }
                    ) {
                        subjects.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.name) },
                                onClick = {
                                    selectedSubject = s
                                    selectedChapter = null
                                    expandedSub = false
                                }
                            )
                        }
                    }
                }

                if (filteredChapters.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedChap,
                        onExpandedChange = { expandedChap = !expandedChap }
                    ) {
                        OutlinedTextField(
                            value = selectedChapter?.name ?: "All Chapters",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Chapter") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedChap) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedChap,
                            onDismissRequest = { expandedChap = false }
                        ) {
                            filteredChapters.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = {
                                        selectedChapter = c
                                        expandedChap = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSelect(selectedSubject?.id, selectedChapter?.id) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
