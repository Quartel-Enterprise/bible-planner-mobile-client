package com.quare.bibleplanner.feature.logout.presentation.model

import org.jetbrains.compose.resources.StringResource

internal sealed interface LogoutUiState {
    data object Idle : LogoutUiState

    data object Loading : LogoutUiState

    /**
     * Logout was aborted because pending changes couldn't be synced; the user can retry or stay
     * signed in. [pendingResource] names what failed to sync (e.g. favorites) and is interpolated
     * into the error message template.
     */
    data class PendingChangesError(
        val pendingResource: StringResource,
    ) : LogoutUiState
}
