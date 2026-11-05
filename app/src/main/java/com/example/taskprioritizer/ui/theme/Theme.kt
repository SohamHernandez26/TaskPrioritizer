package com.example.taskprioritizer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val lightColors = lightColorScheme(
    primary = deepTeal,
    primaryContainer = mistTeal,
    secondary = inkBlue,
    secondaryContainer = softBlue,
    background = warmWhite,
    surface = warmWhite
)
private val darkColors = darkColorScheme(
    primary = softTeal,
    primaryContainer = deepTeal,
    secondary = softBlue,
    background = nightGreen,
    surface = nightGreen
)

@Composable
fun TaskPrioritizerTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) darkColors else lightColors, typography = appTypography, content = content)
}
