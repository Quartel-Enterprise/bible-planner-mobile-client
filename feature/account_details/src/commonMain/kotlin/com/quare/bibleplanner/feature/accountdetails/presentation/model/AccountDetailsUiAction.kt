package com.quare.bibleplanner.feature.accountdetails.presentation.model

sealed interface AccountDetailsUiAction {
    data class ShowSnackbar(
        val message: AccountDetailsMessage,
    ) : AccountDetailsUiAction
}
