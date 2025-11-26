package com.quare.bibleplanner.ui.theme

import androidx.compose.runtime.compositionLocalOf

val LocalThemeOption = compositionLocalOf {
    ThemeOption.SYSTEM
}

enum class ThemeOption {
    LIGHT,
    DARK,
    SYSTEM,
}
