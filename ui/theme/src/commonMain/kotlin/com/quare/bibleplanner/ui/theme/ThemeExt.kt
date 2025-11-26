package com.quare.bibleplanner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

@Composable
fun isAppInDarkTheme(): Boolean =
    when (LocalThemeOption.current) {
        ThemeOption.LIGHT -> false
        ThemeOption.DARK -> true
        ThemeOption.SYSTEM -> isSystemInDarkTheme()
    }
