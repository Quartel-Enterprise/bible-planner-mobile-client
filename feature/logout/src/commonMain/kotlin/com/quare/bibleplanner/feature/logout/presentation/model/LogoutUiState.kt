package com.quare.bibleplanner.feature.logout.presentation.model

import com.quare.bibleplanner.feature.logout.domain.usecase.LogoutPhase
import org.jetbrains.compose.resources.StringResource

internal sealed interface LogoutUiState {
    data object Idle : LogoutUiState

    data class Loading(
        val phase: LogoutPhase,
    ) : LogoutUiState

    /**
     * Logout was aborted because pending changes couldn't be synced; the user can retry or stay
     * signed in. [pendingResource] names what failed to sync (e.g. favorites) and is interpolated
     * into the error message template.
     */
    data class PendingChangesError(
        val pendingResource: StringResource,
    ) : LogoutUiState
}
