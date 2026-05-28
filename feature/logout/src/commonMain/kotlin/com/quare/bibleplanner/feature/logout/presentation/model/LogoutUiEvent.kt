package com.quare.bibleplanner.feature.logout.presentation.model

internal sealed interface LogoutUiEvent {
    data object OnConfirmLogout : LogoutUiEvent

    data object OnCancel : LogoutUiEvent

    data object OnDismiss : LogoutUiEvent
}
