package com.quare.bibleplanner.android.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

private val successLight = Color(0xFF2E7D32)
private val successDark = Color(0xFF4ADE80)

// Why: Material 3 has no success role.
val ColorScheme.success: Color
    get() = if (isDark()) successDark else successLight
