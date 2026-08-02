package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

enum class DayIndicator {
    PLANNED, COMPLETED, EXAM
}

@Composable
fun CalendarView(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    dayIndicators: Map<LocalDate, List<DayIndicator>>,
    modifier: Modifier = Modifier
) {
    val currentMonth = YearMonth.from(selectedDate)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        // Month Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row {
                IconButton(onClick = { onDateSelected(selectedDate.minusMonths(1)) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month"
                    )
                }
                IconButton(onClick = { onDateSelected(selectedDate.plusMonths(1)) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Days of week header
        Row(modifier = Modifier.fillMaxWidth()) {
            val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Days grid
        val firstDayOfMonth = currentMonth.atDay(1)
        val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.value - 1 // 0 for Mon, 6 for Sun
        val daysInMonth = currentMonth.lengthOfMonth()

        var currentDayCounter = 1 - dayOfWeekOffset

        for (week in 0..5) {
            if (currentDayCounter > daysInMonth) break

            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayOfWeek in 0..6) {
                    if (currentDayCounter in 1..daysInMonth) {
                        val date = currentMonth.atDay(currentDayCounter)
                        val isSelected = date == selectedDate
                        val isToday = date == LocalDate.now()
                        val indicators = dayIndicators[date] ?: emptyList()

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )

                                // Indicator Dots
                                if (indicators.isNotEmpty()) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        if (indicators.contains(DayIndicator.EXAM)) {
                                            Text("★", style = MaterialTheme.typography.labelSmall, color = Color(0xFFEF4444), fontSize = 8.dp.value.sp)
                                        } else {
                                            if (indicators.contains(DayIndicator.COMPLETED)) {
                                                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                                            }
                                            if (indicators.contains(DayIndicator.PLANNED)) {
                                                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    currentDayCounter++
                }
            }
        }
    }
}
