package com.quare.bibleplanner.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.quare.bibleplanner.ui.theme.color.darkScheme
import com.quare.bibleplanner.ui.theme.color.lightScheme

@Composable
fun AppTheme(
    getSpecificColors: @Composable ((Boolean) -> ColorScheme?)? = null,
    content: @Composable () -> Unit,
) {
    val isDarkTheme = isAppInDarkTheme()
    MaterialTheme(
        colorScheme = getSpecificColors?.invoke(isDarkTheme) ?: getColorScheme(isDarkTheme),
        content = content,
    )
}

@Composable
private fun getColorScheme(isDarkTheme: Boolean): ColorScheme = if (isDarkTheme) {
    darkScheme
} else {
    lightScheme
}
