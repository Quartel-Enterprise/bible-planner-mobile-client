package com.quare.bibleplanner.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalDynamicColorScheme = staticCompositionLocalOf<@Composable (isDark: Boolean) -> ColorScheme?> {
    { null }
}
