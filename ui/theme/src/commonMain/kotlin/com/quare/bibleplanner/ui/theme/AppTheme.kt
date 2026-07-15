package com.quare.bibleplanner.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import com.quare.bibleplanner.ui.theme.color.darkScheme
import com.quare.bibleplanner.ui.theme.color.highContrastDarkColorScheme
import com.quare.bibleplanner.ui.theme.color.highContrastLightColorScheme
import com.quare.bibleplanner.ui.theme.color.lightScheme
import com.quare.bibleplanner.ui.theme.color.mediumContrastDarkColorScheme
import com.quare.bibleplanner.ui.theme.color.mediumContrastLightColorScheme
import com.quare.bibleplanner.ui.theme.model.ContrastType

@Composable
fun AppTheme(
    getSpecificColors: @Composable ((Boolean) -> ColorScheme?)? = null,
    onThemeResolved: (isDarkTheme: Boolean) -> Unit = {},
    contrastType: ContrastType = ContrastType.Standard,
    content: @Composable () -> Unit,
) {
    val isDarkTheme = isAppInDarkTheme()
    SideEffect { onThemeResolved(isDarkTheme) }
    CompositionLocalProvider(LocalDynamicColorScheme provides (getSpecificColors ?: { null })) {
        MaterialTheme(
            colorScheme = getSpecificColors?.invoke(isDarkTheme) ?: getColorScheme(isDarkTheme, contrastType),
            content = content,
        )
    }
}

@Composable
private fun getColorScheme(
    isDarkTheme: Boolean,
    contrastType: ContrastType,
): ColorScheme = when (contrastType) {
    ContrastType.Standard -> if (isDarkTheme) darkScheme else lightScheme
    ContrastType.Medium -> if (isDarkTheme) mediumContrastDarkColorScheme else mediumContrastLightColorScheme
    ContrastType.High -> if (isDarkTheme) highContrastDarkColorScheme else highContrastLightColorScheme
}
