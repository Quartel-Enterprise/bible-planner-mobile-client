package com.quare.bibleplanner.ui.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

class MainBottomBarState {
    var visibleHeightPx by mutableFloatStateOf(0f)
        internal set
}

val LocalMainBottomBarState = staticCompositionLocalOf { MainBottomBarState() }

/**
 * Reports the height, in pixels, that this screen takes at the bottom of the window, so the
 * root-anchored overlays (the app snackbar, the update banner) sit above it instead of on top of
 * it. Any screen that owns the bottom edge has to say so — the tab bar, the reader's bar, the
 * paywall's call to action, the chat composer — or that overlay lands on it.
 *
 * [heightPx] is read inside a snapshot flow rather than during composition, so a bar that collapses
 * as the user scrolls can feed its live offset in without recomposing the screen on every frame.
 */
@Composable
fun ReserveBottomOverlayHeight(heightPx: () -> Float) {
    val state = LocalMainBottomBarState.current
    LaunchedEffect(state) {
        snapshotFlow(heightPx).collect { height -> state.visibleHeightPx = height }
    }
    DisposableEffect(state) {
        onDispose { state.visibleHeightPx = 0f }
    }
}

@Composable
fun mainContentBottomInset(): Dp {
    val state = LocalMainBottomBarState.current
    val density = LocalDensity.current
    val navigationBarBottom = WindowInsets.navigationBars.getBottom(density).toFloat()
    return with(density) { maxOf(navigationBarBottom, state.visibleHeightPx).toDp() }
}
