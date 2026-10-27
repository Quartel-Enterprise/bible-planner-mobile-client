package com.quare.bibleplanner.feature.accountdetails.presentation.model

internal sealed interface RenameDeviceUiAction {
    data object NotifyRenameFailed : RenameDeviceUiAction
}
