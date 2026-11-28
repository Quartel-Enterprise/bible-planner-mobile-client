package com.quare.bibleplanner.feature.logout.presentation.model

sealed interface LogoutUiAction {
    data class ShowError(
        val error: LogoutError,
    ) : LogoutUiAction

    // Why: emitted right before the dialog navigates back, so the screen may be gone when it shows.
    data object NotifySuccess : LogoutUiAction
}
