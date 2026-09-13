package com.quare.bibleplanner.ui.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection

@Composable
fun WindowInsets.asStable(): WindowInsets {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    return WindowInsets(
        left = getLeft(
            density = density,
            layoutDirection = layoutDirection,
        ),
        top = getTop(density),
        right = getRight(
            density = density,
            layoutDirection = layoutDirection,
        ),
        bottom = getBottom(density),
    )
}
