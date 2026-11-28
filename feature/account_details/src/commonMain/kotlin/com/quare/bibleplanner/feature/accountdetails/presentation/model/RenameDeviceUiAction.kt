package com.quare.bibleplanner.feature.accountdetails.presentation.model

sealed interface RenameDeviceUiAction {
    data object ShowError : RenameDeviceUiAction
}
