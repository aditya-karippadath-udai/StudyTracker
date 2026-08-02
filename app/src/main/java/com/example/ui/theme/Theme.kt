package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun StudyFlowTheme(
    themeMode: String = "Dark",
    accentColor: String = "Blue",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "Light" -> false
        "Dark" -> true
        else -> isSystemInDarkTheme()
    }

    val (primaryColor, secondaryColor, primaryContainer) = when (accentColor) {
        "Green" -> if (darkTheme) Triple(GreenPrimary, GreenSecondary, GreenContainer) else Triple(GreenSecondary, GreenPrimary, LightGreenContainer)
        "Purple" -> if (darkTheme) Triple(PurplePrimary, PurpleSecondary, PurpleContainer) else Triple(PurpleSecondary, PurplePrimary, LightPurpleContainer)
        else -> if (darkTheme) Triple(BluePrimary, BlueSecondary, BlueContainer) else Triple(BlueSecondary, BluePrimary, LightBlueContainer)
    }

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = primaryColor,
            secondary = secondaryColor,
            primaryContainer = primaryContainer,
            background = DarkBackground,
            surface = DarkSurface,
            surfaceVariant = DarkSurfaceVariant,
            surfaceContainerHigh = DarkSurfaceHighlight,
            onBackground = TextPrimaryDark,
            onSurface = TextPrimaryDark,
            onSurfaceVariant = TextSecondaryDark,
            outline = Color(0xFF334155)
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            secondary = secondaryColor,
            primaryContainer = primaryContainer,
            background = LightBackground,
            surface = LightSurface,
            surfaceVariant = LightSurfaceVariant,
            surfaceContainerHigh = LightSurfaceHighlight,
            onBackground = TextPrimaryLight,
            onSurface = TextPrimaryLight,
            onSurfaceVariant = TextSecondaryLight,
            outline = Color(0xFFCBD5E1)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
