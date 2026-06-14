package com.quare.bibleplanner.feature.logout.presentation.model

internal sealed interface LogoutUiEvent {
    data object OnConfirmLogout : LogoutUiEvent

    /** Sign out without flushing pending changes — chosen after a [LogoutUiState.PendingChangesError]. */
    data object OnForceLogout : LogoutUiEvent

    data object OnCancel : LogoutUiEvent

    data object OnDismiss : LogoutUiEvent
}
