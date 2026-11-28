package com.quare.bibleplanner.android.feature.shareverse

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

/*
 * Why: a pause marks leaving the screen on a phone, but in desktop windowing or on ChromeOS the share sheet opens in
 * its own window and the app is never paused; it only loses window focus. Either signal counts, so the guard lifts in
 * both. Call it inside the sheet, whose own window is the one that loses focus.
 */
@Composable
internal fun ShareActionGuardEffect(guard: ShareActionGuard) {
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { guard.onScreenLeft() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { guard.onScreenReturned() }
    val isWindowFocused = LocalWindowInfo.current.isWindowFocused
    LaunchedEffect(guard, isWindowFocused) {
        if (isWindowFocused) guard.onScreenReturned() else guard.onScreenLeft()
    }
}
