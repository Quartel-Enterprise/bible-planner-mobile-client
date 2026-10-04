package com.quare.bibleplanner.ui.utils.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNot

@Composable
fun SheetExitAnimationEffect(
    animateOut: suspend () -> Unit,
    animateBackIn: suspend () -> Unit,
) {
    val sheetExitAnimation = LocalSheetExitAnimation.current ?: return
    val currentAnimateOut by rememberUpdatedState(animateOut)
    val currentAnimateBackIn by rememberUpdatedState(animateBackIn)
    DisposableEffect(sheetExitAnimation) {
        val animation: suspend () -> Unit = { currentAnimateOut() }
        sheetExitAnimation.register(animation)
        onDispose { sheetExitAnimation.unregister(animation) }
    }
    LaunchedEffect(sheetExitAnimation) {
        snapshotFlow { sheetExitAnimation.isExiting }
            .drop(1)
            .filterNot { isExiting -> isExiting }
            .collect { currentAnimateBackIn() }
    }
}
