package com.quare.bibleplanner.feature.deleteaccount.presentation.model

import org.jetbrains.compose.resources.StringResource

internal sealed interface DeleteAccountUiAction {
    data class ShowSnackbar(
        val message: StringResource,
    ) : DeleteAccountUiAction

    data class NotifySuccess(
        val message: StringResource,
    ) : DeleteAccountUiAction
}
