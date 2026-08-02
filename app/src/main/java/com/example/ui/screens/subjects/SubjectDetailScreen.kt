package com.example.ui.screens.subjects

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.TopAppBar
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.SubjectEntity
import com.example.ui.components.ProgressBar
import com.example.ui.components.ProgressRing
import com.example.ui.components.StudyFlowCard
import com.example.ui.components.getIconVector
import com.example.ui.components.parseHexColor
import com.example.ui.theme.PriorityCritical
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    subjectId: Long,
    viewModel: SubjectsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(subjectId) {
        viewModel.selectSubject(subjectId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chapters by viewModel.selectedSubjectChapters.collectAsStateWithLifecycle()

    val subjectWithStats = uiState.subjectsWithStats.find { it.subject.id == subjectId }
    val subject = subjectWithStats?.subject

    var showAddChapterDialog by remember { mutableStateOf(false) }

    if (subject == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Subject not found.")
        }
        return
    }

    val color = parseHexColor(subject.colorHex)
    val iconVector = getIconVector(subject.iconName)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(subject.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.deleteSubject(subject)
                        onBackClick()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Subject", tint = Color(0xFFEF4444))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddChapterDialog = true },
                containerColor = color,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_chapter")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Chapter")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overview Header Card
            item {
                StudyFlowCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = color
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(color.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(iconVector, contentDescription = null, tint = color)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = subject.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (subject.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = subject.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "${subjectWithStats?.completedChapters ?: 0} / ${chapters.size} Chapters Complete",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }

                        ProgressRing(
                            progress = (subjectWithStats?.progressPercent ?: 0) / 100f,
                            size = 80.dp,
                            strokeWidth = 8.dp,
                            activeColor = color,
                            centerText = "${subjectWithStats?.progressPercent ?: 0}%"
                        )
                    }
                }
            }

            item {
                Text(
                    text = "CHAPTERS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (chapters.isEmpty()) {
                item {
                    StudyFlowCard(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No chapters added yet. Tap + to add a chapter!")
                        }
                    }
                }
            } else {
                items(chapters) { chapter ->
                    val priorityColor = when (chapter.priority) {
                        "Critical" -> PriorityCritical
                        "High" -> PriorityHigh
                        "Medium" -> PriorityMedium
                        else -> PriorityLow
                    }

                    StudyFlowCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = if (chapter.isCompleted) Color(0xFF22C55E).copy(alpha = 0.4f) else color.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = chapter.isCompleted,
                                onCheckedChange = { viewModel.toggleChapterCompletion(chapter) },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF22C55E))
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = chapter.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (chapter.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(priorityColor.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = chapter.priority.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = priorityColor
                                        )
                                    }
                                }
                                if (chapter.description.isNotBlank()) {
                                    Text(
                                        text = chapter.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "~${chapter.estimatedMinutes} mins est.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(onClick = { viewModel.deleteChapter(chapter) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Chapter",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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

        if (showAddChapterDialog) {
            AddChapterDialog(
                onDismiss = { showAddChapterDialog = false },
                onConfirm = { name, desc, priority, estMins, notes ->
                    viewModel.addChapter(subjectId, name, desc, priority, estMins, notes)
                    showAddChapterDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChapterDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var estimatedMins by remember { mutableStateOf("45") }
    var notes by remember { mutableStateOf("") }

    var expandedPriority by remember { mutableStateOf(false) }
    val priorities = listOf("Low", "Medium", "High", "Critical")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Chapter", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Chapter Name *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_chapter_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedPriority,
                    onExpandedChange = { expandedPriority = !expandedPriority }
                ) {
                    OutlinedTextField(
                        value = priority,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priority") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPriority,
                        onDismissRequest = { expandedPriority = false }
                    ) {
                        priorities.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p) },
                                onClick = {
                                    priority = p
                                    expandedPriority = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = estimatedMins,
                    onValueChange = { estimatedMins = it },
                    label = { Text("Estimated Duration (Minutes)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val mins = estimatedMins.toIntOrNull() ?: 45
                        onConfirm(name.trim(), description.trim(), priority, mins, notes.trim())
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("button_save_chapter")
            ) {
                Text("Save Chapter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
