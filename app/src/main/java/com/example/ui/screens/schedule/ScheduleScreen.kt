package com.example.ui.screens.schedule

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.ChapterEntity
import com.example.data.local.entities.SubjectEntity
import com.example.ui.components.CalendarView
import com.example.ui.components.EmptyState
import com.example.ui.components.StudyFlowCard
import com.example.ui.components.parseHexColor
import java.time.format.DateTimeFormatter

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    onNavigateToFocus: (subjectId: Long?, chapterId: Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_session")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Session")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Calendar & Schedule",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Calendar
            CalendarView(
                selectedDate = uiState.selectedDate,
                onDateSelected = { viewModel.onDateSelected(it) },
                dayIndicators = uiState.dayIndicators
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SCHEDULE FOR " + uiState.selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.daySessions.isEmpty() && uiState.dayTopicDeadlines.isEmpty()) {
                EmptyState(
                    title = "No Sessions or Topic Deadlines",
                    description = "Tap + to plan a study session for ${uiState.selectedDate.format(DateTimeFormatter.ofPattern("MMM d"))}.",
                    icon = Icons.Default.CalendarToday,
                    actionLabel = "Plan Session",
                    onActionClick = { showAddDialog = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.dayTopicDeadlines.isNotEmpty()) {
                        item {
                            Text(
                                text = "TOPIC DEADLINES TODAY",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        items(uiState.dayTopicDeadlines) { item ->
                            val subjectColor = parseHexColor(item.subject?.colorHex ?: "#2196F3")
                            StudyFlowCard(
                                modifier = Modifier.fillMaxWidth(),
                                borderColor = subjectColor
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(subjectColor.copy(alpha = 0.2f))
                                            .padding(8.dp)
                                    ) {
                                        Text("📌")
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.subject?.name ?: "Subject",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = subjectColor
                                        )
                                        Text(
                                            text = item.chapter.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Study Deadline Today • ~${item.chapter.estimatedMinutes} mins",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { onNavigateToFocus(item.chapter.subjectId, item.chapter.id) },
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

                        if (uiState.daySessions.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "PLANNED SESSIONS",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    items(uiState.daySessions) { session ->
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
                                    onCheckedChange = { viewModel.toggleSessionCompleted(session) },
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
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$timeString • ${session.durationMinutes} minutes",
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

                                IconButton(onClick = { viewModel.deleteSession(session) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
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

        if (showAddDialog) {
            AddSessionDialog(
                subjects = uiState.subjects,
                chapters = uiState.chapters,
                onDismiss = { showAddDialog = false },
                onConfirm = { subjectId, chapterId, title, startMin, duration, priority, notes, repeat ->
                    viewModel.addStudySession(subjectId, chapterId, title, startMin, duration, priority, notes, repeat)
                    showAddDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSessionDialog(
    subjects: List<SubjectEntity>,
    chapters: List<ChapterEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long?, String, Int, Int, String, String, String) -> Unit
) {
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()) }
    var selectedChapter by remember { mutableStateOf<ChapterEntity?>(null) }
    var title by remember { mutableStateOf("") }
    var startTimeStr by remember { mutableStateOf("10:00") }
    var durationStr by remember { mutableStateOf("45") }
    var priority by remember { mutableStateOf("Medium") }
    var notes by remember { mutableStateOf("") }

    var expandedSubject by remember { mutableStateOf(false) }
    var expandedChapter by remember { mutableStateOf(false) }

    val filteredChapters = chapters.filter { it.subjectId == selectedSubject?.id }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Plan Study Session", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Subject Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedSubject,
                    onExpandedChange = { expandedSubject = !expandedSubject }
                ) {
                    OutlinedTextField(
                        value = selectedSubject?.name ?: "Select Subject",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubject) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSubject,
                        onDismissRequest = { expandedSubject = false }
                    ) {
                        subjects.forEach { subj ->
                            DropdownMenuItem(
                                text = { Text(subj.name) },
                                onClick = {
                                    selectedSubject = subj
                                    selectedChapter = null
                                    if (title.isEmpty()) title = "${subj.name} Session"
                                    expandedSubject = false
                                }
                            )
                        }
                    }
                }

                // Chapter Dropdown
                if (filteredChapters.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedChapter,
                        onExpandedChange = { expandedChapter = !expandedChapter }
                    ) {
                        OutlinedTextField(
                            value = selectedChapter?.name ?: "Select Chapter (Optional)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Chapter") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedChapter) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedChapter,
                            onDismissRequest = { expandedChapter = false }
                        ) {
                            filteredChapters.forEach { chap ->
                                DropdownMenuItem(
                                    text = { Text(chap.name) },
                                    onClick = {
                                        selectedChapter = chap
                                        title = chap.name
                                        expandedChapter = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Session Title *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_session_title"),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTimeStr,
                        onValueChange = { startTimeStr = it },
                        label = { Text("Start Time (HH:MM)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = durationStr,
                        onValueChange = { durationStr = it },
                        label = { Text("Duration (mins)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedSubject != null && title.isNotBlank()) {
                        val timeParts = startTimeStr.split(":")
                        val startMin = if (timeParts.size == 2) {
                            (timeParts[0].toIntOrNull() ?: 10) * 60 + (timeParts[1].toIntOrNull() ?: 0)
                        } else 600
                        val dur = durationStr.toIntOrNull() ?: 45

                        onConfirm(
                            selectedSubject!!.id,
                            selectedChapter?.id,
                            title.trim(),
                            startMin,
                            dur,
                            priority,
                            notes,
                            "None"
                        )
                    }
                },
                enabled = selectedSubject != null && title.isNotBlank(),
                modifier = Modifier.testTag("button_save_session")
            ) {
                Text("Save Session")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
