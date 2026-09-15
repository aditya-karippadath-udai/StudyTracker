package com.example.ui.screens.subjects

import android.app.DatePickerDialog
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.NotificationsActive
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.ChapterEntity
import com.example.ui.components.ProgressRing
import com.example.ui.components.StudyFlowCard
import com.example.ui.components.getIconVector
import com.example.ui.components.parseHexColor
import com.example.ui.theme.PriorityCritical
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

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
    var editingChapter by remember { mutableStateOf<ChapterEntity?>(null) }

    if (subject == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Subject not found.")
        }
        return
    }

    val color = parseHexColor(subject.colorHex)
    val iconVector = getIconVector(subject.iconName)
    val todayEpochDay = LocalDate.now().toEpochDay()

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
                Icon(Icons.Default.Add, contentDescription = "Add Topic")
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
                                text = "${subjectWithStats?.completedChapters ?: 0} / ${chapters.size} Topics Complete",
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
                    text = "TOPICS & CHAPTERS TO STUDY",
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
                            Text("No topics created yet. Tap + to create topics & set study deadlines!")
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

                    val deadlineEpoch = chapter.deadlineEpochDay
                    val deadlineDate = deadlineEpoch?.let { LocalDate.ofEpochDay(it) }

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
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = chapter.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "~${chapter.estimatedMinutes} mins est.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (deadlineDate != null) {
                                        val formattedDate = deadlineDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                                        val isToday = deadlineEpoch == todayEpochDay
                                        val isOverdue = deadlineEpoch < todayEpochDay && !chapter.isCompleted

                                        val (bgColor, textColor, labelText, icon) = when {
                                            isToday -> Quadruple(
                                                Color(0xFFFEF3C7),
                                                Color(0xFFD97706),
                                                "Due Today!",
                                                Icons.Default.NotificationsActive
                                            )
                                            isOverdue -> Quadruple(
                                                Color(0xFFFEE2E2),
                                                Color(0xFFDC2626),
                                                "Overdue ($formattedDate)",
                                                Icons.Default.Event
                                            )
                                            else -> Quadruple(
                                                MaterialTheme.colorScheme.surfaceVariant,
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                                "Deadline: $formattedDate",
                                                Icons.Default.Event
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(bgColor)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = textColor,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = labelText,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            IconButton(onClick = { editingChapter = chapter }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Topic",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }

                            IconButton(onClick = { viewModel.deleteChapter(chapter) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Topic",
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
                onConfirm = { name, desc, priority, estMins, notes, deadline ->
                    viewModel.addChapter(subjectId, name, desc, priority, estMins, notes, deadline)
                    showAddChapterDialog = false
                }
            )
        }

        editingChapter?.let { chapterToEdit ->
            EditChapterDialog(
                chapter = chapterToEdit,
                onDismiss = { editingChapter = null },
                onConfirm = { updated ->
                    viewModel.updateChapter(updated)
                    editingChapter = null
                }
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChapterDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, String, Long?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var estimatedMins by remember { mutableStateOf("45") }
    var notes by remember { mutableStateOf("") }
    var deadlineEpochDay by remember { mutableStateOf<Long?>(null) }

    var expandedPriority by remember { mutableStateOf(false) }
    val priorities = listOf("Low", "Medium", "High", "Critical")

    val context = LocalContext.current

    val deadlineText = deadlineEpochDay?.let {
        LocalDate.ofEpochDay(it).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    } ?: "Set Deadline (Optional)"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Topic to Study", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Topic / Chapter Name *") },
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

                // Deadline Selector Button
                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        deadlineEpochDay?.let {
                            val date = LocalDate.ofEpochDay(it)
                            calendar.set(date.year, date.monthValue - 1, date.dayOfMonth)
                        }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                                deadlineEpochDay = selectedDate.toEpochDay()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = deadlineText,
                                fontWeight = if (deadlineEpochDay != null) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        if (deadlineEpochDay != null) {
                            IconButton(
                                onClick = { deadlineEpochDay = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Deadline", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

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
                        onConfirm(name.trim(), description.trim(), priority, mins, notes.trim(), deadlineEpochDay)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("button_save_chapter")
            ) {
                Text("Save Topic")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditChapterDialog(
    chapter: ChapterEntity,
    onDismiss: () -> Unit,
    onConfirm: (ChapterEntity) -> Unit
) {
    var name by remember { mutableStateOf(chapter.name) }
    var description by remember { mutableStateOf(chapter.description) }
    var priority by remember { mutableStateOf(chapter.priority) }
    var estimatedMins by remember { mutableStateOf(chapter.estimatedMinutes.toString()) }
    var notes by remember { mutableStateOf(chapter.notes) }
    var deadlineEpochDay by remember { mutableStateOf<Long?>(chapter.deadlineEpochDay) }

    var expandedPriority by remember { mutableStateOf(false) }
    val priorities = listOf("Low", "Medium", "High", "Critical")

    val context = LocalContext.current

    val deadlineText = deadlineEpochDay?.let {
        LocalDate.ofEpochDay(it).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    } ?: "Set Deadline (Optional)"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Topic", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Topic Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Deadline Selector Button
                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        deadlineEpochDay?.let {
                            val date = LocalDate.ofEpochDay(it)
                            calendar.set(date.year, date.monthValue - 1, date.dayOfMonth)
                        }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                                deadlineEpochDay = selectedDate.toEpochDay()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = deadlineText,
                                fontWeight = if (deadlineEpochDay != null) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        if (deadlineEpochDay != null) {
                            IconButton(
                                onClick = { deadlineEpochDay = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Deadline", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

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
                        val mins = estimatedMins.toIntOrNull() ?: chapter.estimatedMinutes
                        onConfirm(
                            chapter.copy(
                                name = name.trim(),
                                description = description.trim(),
                                priority = priority,
                                estimatedMinutes = mins,
                                notes = notes.trim(),
                                deadlineEpochDay = deadlineEpochDay
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
