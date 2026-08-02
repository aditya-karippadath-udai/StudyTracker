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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.example.ui.components.ColorSelector
import com.example.ui.components.EmptyState
import com.example.ui.components.IconSelector
import com.example.ui.components.ProgressBar
import com.example.ui.components.StudyFlowCard
import com.example.ui.components.getIconVector
import com.example.ui.components.parseHexColor

@Composable
fun SubjectsScreen(
    viewModel: SubjectsViewModel,
    onSubjectClick: (Long) -> Unit,
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
                modifier = Modifier.testTag("fab_add_subject")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Subject")
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
                text = "Subjects",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search subjects...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_subjects"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.subjectsWithStats.isEmpty()) {
                EmptyState(
                    title = "📚 No Subjects Found",
                    description = if (uiState.searchQuery.isNotBlank()) "No subjects match '${uiState.searchQuery}'." else "Add your first subject and start planning your studies.",
                    icon = Icons.Default.Book,
                    actionLabel = "Add Subject",
                    onActionClick = { showAddDialog = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.subjectsWithStats) { item ->
                        val subject = item.subject
                        val color = parseHexColor(subject.colorHex)
                        val iconVector = getIconVector(subject.iconName)

                        StudyFlowCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onSubjectClick(subject.id) },
                            borderColor = color.copy(alpha = 0.5f),
                            testTag = "subject_card_${subject.id}"
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(color.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = iconVector,
                                            contentDescription = null,
                                            tint = color,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = subject.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (subject.description.isNotBlank()) {
                                            Text(
                                                text = subject.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "${item.totalChapters} Chapters • ${item.completedChapters} Completed",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(color.copy(alpha = 0.2f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${item.progressPercent}%",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = color
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                ProgressBar(
                                    progress = item.progressPercent / 100f,
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

        if (showAddDialog) {
            AddSubjectDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, desc, colorHex, iconName, targetHours ->
                    viewModel.addSubject(name, desc, colorHex, iconName, targetHours)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Float) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#2196F3") }
    var selectedIconName by remember { mutableStateOf("School") }
    var targetHours by remember { mutableStateOf("5") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Subject", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_subject_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Subject Color", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                ColorSelector(
                    selectedColorHex = selectedColorHex,
                    onColorSelected = { selectedColorHex = it }
                )

                Text("Subject Icon", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                IconSelector(
                    selectedIconName = selectedIconName,
                    onIconSelected = { selectedIconName = it }
                )

                OutlinedTextField(
                    value = targetHours,
                    onValueChange = { targetHours = it },
                    label = { Text("Target Study Hours / Week") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val hours = targetHours.toFloatOrNull() ?: 5.0f
                        onConfirm(name.trim(), description.trim(), selectedColorHex, selectedIconName, hours)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("button_save_subject")
            ) {
                Text("Save Subject")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
