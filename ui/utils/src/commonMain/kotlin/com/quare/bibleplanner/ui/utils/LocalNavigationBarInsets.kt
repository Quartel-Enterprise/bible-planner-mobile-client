package com.quare.bibleplanner.ui.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.staticCompositionLocalOf

val LocalNavigationBarInsets = staticCompositionLocalOf<WindowInsets> {
    error("LocalNavigationBarInsets not provided")
}
