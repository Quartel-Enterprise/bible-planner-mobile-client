package com.quare.bibleplanner.ui.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.jetbrains.compose.resources.StringResource

/**
 * App-wide snackbar channel: features push a message and the root scaffold shows it on the
 * single [LocalSnackbarHostState]. Use it when the emitting screen may leave composition
 * before the snackbar is shown (e.g. the login sheet closing on success).
 */
class AppSnackbarController {
    private val _messages: MutableSharedFlow<StringResource> = MutableSharedFlow(extraBufferCapacity = 1)
    val messages: SharedFlow<StringResource> = _messages

    fun show(message: StringResource) {
        _messages.tryEmit(message)
    }
}
