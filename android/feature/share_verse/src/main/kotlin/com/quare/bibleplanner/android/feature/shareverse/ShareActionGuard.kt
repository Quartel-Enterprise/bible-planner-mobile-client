package com.quare.bibleplanner.android.feature.shareverse

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/*
 * Why: the share sheet and the saver take a moment to show up, and a second tap meanwhile would send the passage or
 * capture the card twice. No timer decides when they lift: sharing lifts once the user has left the screen for the
 * share sheet and come back (see ShareActionGuardEffect), or at once when the launch fails; saving lifts on the saver's
 * result, or on the same return, since the file dialog before Android 10 can end without reporting one.
 */
@Stable
internal class ShareActionGuard {
    var isSharing: Boolean by mutableStateOf(false)
    var isSaving: Boolean by mutableStateOf(false)
    private var hasLeftScreen = false

    val isBusy: Boolean
        get() = isSharing || isSaving

    fun onScreenLeft() {
        if (isBusy) hasLeftScreen = true
    }

    fun onScreenReturned() {
        if (!hasLeftScreen) return
        hasLeftScreen = false
        isSharing = false
        isSaving = false
    }
}
