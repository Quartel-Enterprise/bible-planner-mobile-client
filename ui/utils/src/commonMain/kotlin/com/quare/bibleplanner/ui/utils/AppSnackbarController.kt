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
        field = MutableSharedFlow<AppSnackbarMessage>(extraBufferCapacity = 1)

    fun show(message: AppSnackbarMessage) {
        messages.tryEmit(message)
    }
}
