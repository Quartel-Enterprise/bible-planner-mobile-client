package com.quare.bibleplanner.feature.books.presentation.utils

import androidx.compose.animation.ExperimentalSharedTransitionApi
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

/**
 * An [SharedTransitionScope.OverlayClip] that returns null for the clip path,
 * effectively disabling clipping in the overlay during transitions.
 * This is useful for preserving shadows (like those on ElevatedCard) that
 * would otherwise be clipped by the default bounds-based clipping.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val NoClip = object : SharedTransitionScope.OverlayClip {
    override fun getClipPath(
        sharedContentState: SharedTransitionScope.SharedContentState,
        bounds: Rect,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Path? = null
}

/**
 * A specialized [OverlayClip] for cards with shadows.
 * It uses a 16dp margin to ensure shadows are not clipped.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun getShadowClip(shape: Shape) = OverlayClip(shape, padding = 16.dp)

/**
 * An [SharedTransitionScope.OverlayClip] that clips to a specific [Shape] with optional padding.
 * Padding is useful to maintain the shape while allowing shadows to be visible.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
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
