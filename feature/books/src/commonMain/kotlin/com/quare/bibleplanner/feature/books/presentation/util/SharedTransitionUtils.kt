package com.quare.bibleplanner.feature.books.presentation.util

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

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
        density: Density
    ): Path? = null
}
