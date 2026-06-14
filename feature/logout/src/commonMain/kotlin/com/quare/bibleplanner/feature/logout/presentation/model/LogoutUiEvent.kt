package com.quare.bibleplanner.feature.logout.presentation.model

internal sealed interface LogoutUiEvent {
    sealed interface ConfirmLogoutClick : LogoutUiEvent {
        val shouldFlushPending: Boolean

        data object OnConfirmLogout : ConfirmLogoutClick {
            override val shouldFlushPending: Boolean = true
        }

        /** Sign out without flushing pending changes — chosen after a [LogoutUiState.PendingChangesError]. */
        data object OnForceLogout : ConfirmLogoutClick {
            override val shouldFlushPending: Boolean = false
        }
    }

    data object OnCancel : LogoutUiEvent

    data object OnDismiss : LogoutUiEvent
}
