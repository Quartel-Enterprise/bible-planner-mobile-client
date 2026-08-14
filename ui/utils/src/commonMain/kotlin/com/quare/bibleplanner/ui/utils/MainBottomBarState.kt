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
