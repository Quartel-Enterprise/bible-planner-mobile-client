package com.quare.bibleplanner.ui.utils.sheet

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

@Stable
class SheetExitAnimation {
    var isExiting: Boolean by mutableStateOf(false)
        private set

    private var registeredAnimation: (suspend () -> Unit)? = null

    fun register(animation: suspend () -> Unit) {
        registeredAnimation = animation
    }

    fun unregister(animation: suspend () -> Unit) {
        if (registeredAnimation === animation) {
            registeredAnimation = null
        }
    }

    suspend fun play() {
        isExiting = true
        try {
            registeredAnimation?.invoke()
        } catch (exception: CancellationException) {
            /*
             * Why: an exit animation interrupted by another one is cancelled while this coroutine
             * is not, and rethrowing would keep the popped sheet's window on screen for good.
             */
            if (currentCoroutineContext().isActive) return
            /*
             * Why: the exit itself is cancelled when the same route is pushed back while the sheet
             * is still leaving, and the sheet on screen is then the one the user opened again.
             */
            isExiting = false
            throw exception
        }
    }
}
