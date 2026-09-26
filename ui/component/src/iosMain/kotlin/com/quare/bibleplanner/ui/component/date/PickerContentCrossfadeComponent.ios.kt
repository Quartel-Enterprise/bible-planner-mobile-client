@file:OptIn(ExperimentalForeignApi::class)

package com.quare.bibleplanner.ui.component.date

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIView

private const val CROSSFADE_DURATION_SECONDS = 0.25

@Composable
actual fun <T> PickerContentCrossfadeComponent(
    targetState: T,
    content: @Composable (T) -> Unit,
) {
    val hostView = LocalUIViewController.current.view
    val lastState = remember { LastState(targetState) }
    SideEffect {
        if (lastState.value != targetState) {
            lastState.value = targetState
            hostView.crossfadeFromSnapshot()
        }
    }
    content(targetState)
}

private class LastState<T>(
    var value: T,
)

private fun UIView.crossfadeFromSnapshot() {
    val snapshot = snapshotViewAfterScreenUpdates(false) ?: return
    snapshot.setFrame(bounds)
    snapshot.userInteractionEnabled = false
    addSubview(snapshot)
    UIView.animateWithDuration(
        duration = CROSSFADE_DURATION_SECONDS,
        animations = { snapshot.alpha = 0.0 },
        completion = { snapshot.removeFromSuperview() },
    )
}
