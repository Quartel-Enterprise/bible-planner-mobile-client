package com.quare.bibleplanner.android.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

private val successLight = Color(0xFF2E7D32)
private val successDark = Color(0xFF4ADE80)
private const val DARK_SURFACE_LUMINANCE = 0.5f

// Why: Material 3 has no success role, and the app's theme setting can differ from the system's, so the surface tells.
val ColorScheme.success: Color
    get() = if (surface.luminance() < DARK_SURFACE_LUMINANCE) successDark else successLight
