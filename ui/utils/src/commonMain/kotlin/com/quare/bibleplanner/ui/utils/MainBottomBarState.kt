package com.quare.bibleplanner.ui.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

class MainBottomBarState {
    var visibleHeightPx by mutableFloatStateOf(0f)
        internal set
}

val LocalMainBottomBarState = staticCompositionLocalOf { MainBottomBarState() }

/**
 * Reports [height] as taken at the bottom of the window while this composition is alive, so the
 * root-anchored overlays (the app snackbar, the update banner) sit above it instead of on top of
 * it. The bottom tab bar reports itself; any other screen that owns the bottom edge — the chat
 * composer, for one — has to say so too, or its input gets covered.
 */
@Composable
fun ReserveBottomOverlayHeight(height: Dp) {
    val state = LocalMainBottomBarState.current
    val heightPx = with(LocalDensity.current) { height.toPx() }
    DisposableEffect(state, heightPx) {
        state.visibleHeightPx = heightPx
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
