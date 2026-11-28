package com.quare.bibleplanner.feature.deleteaccount.presentation.model

sealed interface DeleteAccountUiAction {
    data object ShowError : DeleteAccountUiAction

    data object NotifySuccess : DeleteAccountUiAction
}
