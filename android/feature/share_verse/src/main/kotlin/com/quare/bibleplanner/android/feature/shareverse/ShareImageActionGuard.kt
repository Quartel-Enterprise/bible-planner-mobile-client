package com.quare.bibleplanner.android.feature.shareverse

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/*
 * Why: the share sheet and the saver take a moment to show up, and a second tap meanwhile would capture and send
 * the card twice. Sharing lifts when the user comes back to the app, saving when the saver reports its result.
 */
@Stable
internal class ShareImageActionGuard {
    var isSharing: Boolean by mutableStateOf(false)
    var isSaving: Boolean by mutableStateOf(false)

    val isBusy: Boolean
        get() = isSharing || isSaving
}
