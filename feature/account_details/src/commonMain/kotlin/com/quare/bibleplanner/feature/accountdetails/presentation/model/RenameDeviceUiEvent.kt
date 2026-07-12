package com.quare.bibleplanner.feature.accountdetails.presentation.model

internal sealed interface RenameDeviceUiEvent {
    data class OnConfirmClick(
        val newName: String,
    ) : RenameDeviceUiEvent

    data object OnDismiss : RenameDeviceUiEvent
}
