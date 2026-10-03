package com.quare.bibleplanner.ui.utils

import com.quare.bibleplanner.ui.utils.model.AppSnackbarMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class AppSnackbarController {
    val messages: SharedFlow<AppSnackbarMessage>
        field = MutableSharedFlow<AppSnackbarMessage>(extraBufferCapacity = 1)

    fun show(message: AppSnackbarMessage) {
        messages.tryEmit(message)
    }
}
