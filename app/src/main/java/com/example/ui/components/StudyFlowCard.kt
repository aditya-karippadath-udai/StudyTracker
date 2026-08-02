package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun StudyFlowCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderColor: Color? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    cornerRadius: Dp = 16.dp,
    testTag: String? = null,
    content: @Composable () -> Unit
) {
    var cardModifier = modifier
    if (testTag != null) {
        cardModifier = cardModifier.testTag(testTag)
    }

    Card(
        modifier = if (onClick != null) cardModifier.clickable { onClick() } else cardModifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor ?: MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
