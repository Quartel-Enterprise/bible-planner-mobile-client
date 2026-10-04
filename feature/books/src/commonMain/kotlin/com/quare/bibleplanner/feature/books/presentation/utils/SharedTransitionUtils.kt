package com.quare.bibleplanner.feature.books.presentation.utils

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

// Why: the default bounds-based overlay clip cuts off ElevatedCard shadows during
// shared transitions.
val NoClip = object : SharedTransitionScope.OverlayClip {
    override fun getClipPath(
        sharedContentState: SharedTransitionScope.SharedContentState,
        bounds: Rect,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Path? = null
}

fun getShadowClip(shape: Shape) = OverlayClip(shape, padding = 16.dp)

private fun OverlayClip(
    shape: Shape,
    padding: Dp = 0.dp,
): SharedTransitionScope.OverlayClip = object : SharedTransitionScope.OverlayClip {
    override fun getClipPath(
        sharedContentState: SharedTransitionScope.SharedContentState,
        bounds: Rect,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Path {
        val paddingPx = with(density) { padding.toPx() }
        val sizeWithPadding = Size(
            bounds.width + paddingPx * 2,
            bounds.height + paddingPx * 2,
        )

        return Path().apply {
            addOutline(shape.createOutline(sizeWithPadding, layoutDirection, density))
            translate(Offset(-paddingPx, -paddingPx))
        }
    }
}
