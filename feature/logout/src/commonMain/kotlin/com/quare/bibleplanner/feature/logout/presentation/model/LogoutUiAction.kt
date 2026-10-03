package com.quare.bibleplanner.feature.logout.presentation.model

import org.jetbrains.compose.resources.StringResource

internal sealed interface LogoutUiAction {
    data class ShowSnackbar(
        val message: StringResource,
    ) : LogoutUiAction

    // Why: emitted right before the dialog navigates back, so the screen may be gone when it shows.
    data class NotifySuccess(
        val message: StringResource,
    ) : LogoutUiAction
}
