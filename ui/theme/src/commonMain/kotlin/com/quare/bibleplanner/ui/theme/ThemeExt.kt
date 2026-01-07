package com.quare.bibleplanner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.quare.bibleplanner.ui.theme.color.goldDark
import com.quare.bibleplanner.ui.theme.color.goldLight
import com.quare.bibleplanner.ui.theme.color.successDark
import com.quare.bibleplanner.ui.theme.color.successLight
import com.quare.bibleplanner.ui.theme.model.LocalTheme
import com.quare.bibleplanner.ui.theme.model.Theme

@Composable
fun isAppInDarkTheme(): Boolean = when (LocalTheme.current) {
    Theme.LIGHT -> false
    Theme.DARK -> true
    Theme.SYSTEM -> isSystemInDarkTheme()
}

val MaterialTheme.gold: Color
    @Composable
    get() = if (isAppInDarkTheme()) goldDark else goldLight

val MaterialTheme.success: Color
    @Composable
    get() = if (isAppInDarkTheme()) successDark else successLight
