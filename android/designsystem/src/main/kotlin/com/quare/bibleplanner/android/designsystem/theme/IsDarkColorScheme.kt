package com.quare.bibleplanner.android.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.luminance

private const val DARK_SURFACE_LUMINANCE = 0.5f

// Why: the app's theme setting can differ from the system's, so the scheme's surface tells whether it is dark.
fun ColorScheme.isDark(): Boolean = surface.luminance() < DARK_SURFACE_LUMINANCE
