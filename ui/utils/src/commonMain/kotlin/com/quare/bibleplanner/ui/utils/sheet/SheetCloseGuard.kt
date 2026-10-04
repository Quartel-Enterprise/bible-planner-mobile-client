package com.quare.bibleplanner.ui.utils.sheet

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Stable
class SheetCloseGuard(
    private val sheetExitAnimation: SheetExitAnimation?,
) {
    private val pendingCloseTimeout = 1.seconds

    var hasPendingClose: Boolean by mutableStateOf(false)
        private set

    val isClosing: Boolean
        get() = hasPendingClose || sheetExitAnimation?.isExiting == true

    fun close(onClose: () -> Unit) {
        if (isClosing) return
        hasPendingClose = true
        onClose()
    }

    /*
     * Why: a close the screen answers without popping its route would otherwise leave the sheet
     * refusing every later close; once the pop lands, isExiting takes over from the pending close.
     */
    suspend fun expirePendingClose() {
        delay(pendingCloseTimeout)
        hasPendingClose = false
    }
}
