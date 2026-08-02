package com.example.ui.screens.exams

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.SubjectEntity
import com.example.ui.components.EmptyState
import com.example.ui.components.ProgressBar
import com.example.ui.components.StudyFlowCard
import com.example.ui.components.parseHexColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun ExamsScreen(
    viewModel: ExamsViewModel,
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
                modifier = Modifier.testTag("fab_add_exam")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Exam")
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
                text = "Exam Countdowns",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.examList.isEmpty()) {
                EmptyState(
                    title = "🎯 No Upcoming Exams",
                    description = "Add an exam to start your live countdown and smart preparation planner.",
                    icon = Icons.Default.Event,
                    actionLabel = "Add Exam",
                    onActionClick = { showAddDialog = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.examList) { item ->
                        val exam = item.exam
                        val subject = item.subject
                        val color = parseHexColor(subject?.colorHex ?: "#8B5CF6")
                        val examDate = LocalDate.ofEpochDay(exam.examDateEpochDay)
                        val daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), examDate)

                        val statusText = when {
                            daysLeft < 0 -> "Completed"
                            daysLeft == 0L -> "TODAY!"
                            daysLeft == 1L -> "TOMORROW"
                            else -> "$daysLeft DAYS LEFT"
                        }

                        val statusColor = when {
                            daysLeft <= 3 -> Color(0xFFEF4444)
                            daysLeft <= 7 -> Color(0xFFF97316)
                            else -> color
                        }

                        StudyFlowCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = statusColor.copy(alpha = 0.6f)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = subject?.name ?: "Subject",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = color
                                        )
                                        Text(
                                            text = exam.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = examDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(statusColor.copy(alpha = 0.2f))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = statusText,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor
                                        )
                                    }

                                    IconButton(onClick = { viewModel.deleteExam(exam) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Smart Study Recommendation Card
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = null,
                                            tint = Color(0xFFFBBF24),
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Smart Study Plan Assistant",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = item.recommendation.suggestedScheduleText,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
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

        if (showAddDialog) {
            AddExamDialog(
                subjects = uiState.subjects,
                onDismiss = { showAddDialog = false },
                onConfirm = { subjectId, name, daysOffset, location, notes ->
                    val examDate = LocalDate.now().plusDays(daysOffset.toLong()).toEpochDay()
                    viewModel.addExam(subjectId, name, examDate, location, notes)
                    showAddDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExamDialog(
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Long, String, Int, String, String) -> Unit
) {
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()) }
    var name by remember { mutableStateOf("") }
    var daysInFuture by remember { mutableStateOf("14") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var expandedSub by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Exam", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expandedSub,
                    onExpandedChange = { expandedSub = !expandedSub }
                ) {
                    OutlinedTextField(
                        value = selectedSubject?.name ?: "Select Subject",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject *") },
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
                                    if (name.isEmpty()) name = "${s.name} Final Exam"
                                    expandedSub = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exam Name *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_exam_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = daysInFuture,
                    onValueChange = { daysInFuture = it },
                    label = { Text("Days From Today") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location (e.g. Hall 4B)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedSubject != null && name.isNotBlank()) {
                        val days = daysInFuture.toIntOrNull() ?: 14
                        onConfirm(selectedSubject!!.id, name.trim(), days, location.trim(), notes.trim())
                    }
                },
                enabled = selectedSubject != null && name.isNotBlank(),
                modifier = Modifier.testTag("button_save_exam")
            ) {
                Text("Save Exam")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
