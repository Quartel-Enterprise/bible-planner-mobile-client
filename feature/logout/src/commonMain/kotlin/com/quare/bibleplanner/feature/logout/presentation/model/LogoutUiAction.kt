package com.quare.bibleplanner.feature.logout.presentation.model

import org.jetbrains.compose.resources.StringResource

internal sealed interface LogoutUiAction {
    data class ShowSnackbar(
        val message: StringResource,
    ) : LogoutUiAction

    data class NotifySuccess(
        val message: StringResource,
    ) : LogoutUiAction
}
