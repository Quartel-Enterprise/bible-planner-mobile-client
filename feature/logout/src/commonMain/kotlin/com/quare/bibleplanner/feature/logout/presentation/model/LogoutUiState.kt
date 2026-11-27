package com.quare.bibleplanner.feature.logout.presentation.model

import com.quare.bibleplanner.feature.logout.domain.usecase.LogoutPhase

internal sealed interface LogoutUiState {
    data object Idle : LogoutUiState

    data class Loading(
        val phase: LogoutPhase,
    ) : LogoutUiState

    // Why: unsynced favorites are the only pending changes a logout can lose, so the UI names them itself.
    data object PendingChangesError : LogoutUiState
}
