package com.quare.bibleplanner.feature.accountdetails.presentation.model

internal sealed interface AccountDetailsUiEvent {
    data object OnToggleDevices : AccountDetailsUiEvent

    data class OnRenameDeviceClick(
        val device: DeviceUiModel,
    ) : AccountDetailsUiEvent

    data class OnSignOutDeviceClick(
        val device: DeviceUiModel,
    ) : AccountDetailsUiEvent

    data object OnLogoutClick : AccountDetailsUiEvent
}
