package com.quare.bibleplanner.ui.utils

import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/*
 * Why: lets a screen that may leave composition before the snackbar shows (e.g. the login sheet
 * closing on success) still surface it through the root scaffold's single host state.
 */
class AppSnackbarController {
    val messages: SharedFlow<AppSnackbarMessage>
        // Why: messages that arrive while one is showing wait their turn instead of being dropped.
        field = MutableSharedFlow<AppSnackbarMessage>(extraBufferCapacity = MESSAGE_QUEUE_SIZE)

    fun show(message: AppSnackbarMessage) {
        messages.tryEmit(message)
    }

    private companion object {
        const val MESSAGE_QUEUE_SIZE = 8
    }
}
