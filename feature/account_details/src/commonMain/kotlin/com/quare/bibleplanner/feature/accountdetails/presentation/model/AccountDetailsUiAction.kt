package com.quare.bibleplanner.feature.accountdetails.presentation.model

import org.jetbrains.compose.resources.StringResource

internal sealed interface AccountDetailsUiAction {
    data class ShowSnackbar(
        val message: StringResource,
    ) : AccountDetailsUiAction
}
