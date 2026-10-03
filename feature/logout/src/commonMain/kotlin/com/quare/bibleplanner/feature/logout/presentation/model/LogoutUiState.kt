package com.quare.bibleplanner.feature.logout.presentation.model

import com.quare.bibleplanner.feature.logout.domain.usecase.LogoutPhase
import org.jetbrains.compose.resources.StringResource

internal sealed interface LogoutUiState {
    data object Idle : LogoutUiState

    data class Loading(
        val phase: LogoutPhase,
    ) : LogoutUiState

    data class PendingChangesError(
        val pendingResource: StringResource,
    ) : LogoutUiState
}
