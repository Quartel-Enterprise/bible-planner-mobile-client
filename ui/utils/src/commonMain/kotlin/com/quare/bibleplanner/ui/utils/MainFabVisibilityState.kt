package com.quare.bibleplanner.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

class MainFabVisibilityState {
    private var hideRequestCount by mutableIntStateOf(0)

    val isVisible: Boolean
        get() = hideRequestCount == 0

    internal fun requestHide() {
        hideRequestCount++
    }

    internal fun releaseHide() {
        hideRequestCount--
    }
}

val LocalMainFabVisibilityState = staticCompositionLocalOf { MainFabVisibilityState() }

@Composable
fun HideMainFabEffect() {
    val state = LocalMainFabVisibilityState.current
    DisposableEffect(state) {
        state.requestHide()
        onDispose { state.releaseHide() }
    }
}
