package com.quare.bibleplanner.ui.theme.model

import androidx.compose.runtime.compositionLocalOf

val LocalTheme = compositionLocalOf {
    Theme.SYSTEM
}

enum class Theme {
    LIGHT,
    DARK,
    SYSTEM,
}
