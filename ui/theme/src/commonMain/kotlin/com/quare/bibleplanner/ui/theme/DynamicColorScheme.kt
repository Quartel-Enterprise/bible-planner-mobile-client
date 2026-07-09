package com.quare.bibleplanner.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Resolves the platform's dynamic color scheme (e.g. Android Material You) for a given dark/light
 * mode, independent of the app's current theme. Returns null when dynamic colors are unsupported,
 * disabled, or on platforms without dynamic color, in which case callers fall back to the app's
 * static color scheme.
 */
val LocalDynamicColorScheme = staticCompositionLocalOf<@Composable (isDark: Boolean) -> ColorScheme?> {
    { null }
}
