package com.example.ui.screens.settings

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.example.ui.components.StudyFlowCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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
            // Profile Name Section
            item {
                Text(
                    text = "PROFILE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                StudyFlowCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedTextField(
                            value = settings.userName,
                            onValueChange = { viewModel.updateUserName(it) },
                            label = { Text("Your Name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            // Appearance Section
            item {
                Text(
                    text = "APPEARANCE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                StudyFlowCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Theme Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("Dark", "Light", "System").forEach { mode ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { viewModel.updateThemeMode(mode) }
                                ) {
                                    RadioButton(
                                        selected = settings.themeMode == mode,
                                        onClick = { viewModel.updateThemeMode(mode) },
                                        modifier = Modifier.testTag("radio_theme_$mode")
                                    )
                                    Text(mode, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ColorLens, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Accent Color", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("Blue", "Green", "Purple").forEach { accent ->
                                val color = when (accent) {
                                    "Green" -> Color(0xFF22C55E)
                                    "Purple" -> Color(0xFF8B5CF6)
                                    else -> Color(0xFF2196F3)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { viewModel.updateAccentColor(accent) }
                                ) {
                                    RadioButton(
                                        selected = settings.accentColor == accent,
                                        onClick = { viewModel.updateAccentColor(accent) }
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(accent, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }

            // Daily Study Goal Section
            item {
                Text(
                    text = "STUDY GOALS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                StudyFlowCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Daily Study Goal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(60, 120, 180, 240).forEach { mins ->
                                val hours = mins / 60
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { viewModel.updateDailyGoal(mins) }
                                ) {
                                    RadioButton(
                                        selected = settings.dailyStudyGoalMinutes == mins,
                                        onClick = { viewModel.updateDailyGoal(mins) }
                                    )
                                    Text("${hours}h", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }

            // Notifications Section
            item {
                Text(
                    text = "NOTIFICATIONS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                StudyFlowCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Study Session Reminders", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            }
                            Switch(
                                checked = settings.notificationsStudyEnabled,
                                onCheckedChange = {
                                    viewModel.updateNotificationSettings(
                                        studyEnabled = it,
                                        examEnabled = settings.notificationsExamEnabled,
                                        dailyEnabled = settings.notificationsDailyEnabled
                                    )
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Exam Countdown Reminders", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            }
                            Switch(
                                checked = settings.notificationsExamEnabled,
                                onCheckedChange = {
                                    viewModel.updateNotificationSettings(
                                        studyEnabled = settings.notificationsStudyEnabled,
                                        examEnabled = it,
                                        dailyEnabled = settings.notificationsDailyEnabled
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Data Reset Section
            item {
                Text(
                    text = "DATA MANAGEMENT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                StudyFlowCard(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = Color(0xFFEF4444).copy(alpha = 0.5f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Reset App Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                            Text("Clear all local subjects, schedules, exams, and statistics", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // About Section
            item {
                StudyFlowCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("StudyFlow", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Version 1.0.0 • Offline-First Productivity", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Reset All Data?", fontWeight = FontWeight.Bold) },
                text = { Text("This will permanently remove all your subjects, chapters, schedules, and progress history. Are you sure?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetAllData()
                            showResetDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Reset Everything")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
