package com.quare.bibleplanner.feature.logout.presentation.model

internal sealed interface LogoutUiState {
    data object Idle : LogoutUiState

    data object Loading : LogoutUiState
}
