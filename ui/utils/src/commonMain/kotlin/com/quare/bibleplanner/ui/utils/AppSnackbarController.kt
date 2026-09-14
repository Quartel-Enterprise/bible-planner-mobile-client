package com.quare.bibleplanner.ui.utils

import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * App-wide snackbar channel: features push a message and the root scaffold shows it on the
 * single [LocalSnackbarHostState]. Use it when the emitting screen may leave composition
 * before the snackbar is shown (e.g. the login sheet closing on success).
 */
class AppSnackbarController {
    val messages: SharedFlow<AppSnackbarMessage>
        field = MutableSharedFlow<AppSnackbarMessage>(extraBufferCapacity = 1)

    fun show(message: AppSnackbarMessage) {
        messages.tryEmit(message)
    }
}
