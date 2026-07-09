package com.quare.bibleplanner.feature.logout.presentation.model

import org.jetbrains.compose.resources.StringResource

internal sealed interface LogoutUiAction {
    data object NavigateBack : LogoutUiAction

    data class ShowSnackbar(
        val message: StringResource,
    ) : LogoutUiAction

    /** Emitted right before [NavigateBack] on a successful logout; the screen may already be gone by the time it's shown. */
    data class NotifySuccess(
        val message: StringResource,
    ) : LogoutUiAction
}
