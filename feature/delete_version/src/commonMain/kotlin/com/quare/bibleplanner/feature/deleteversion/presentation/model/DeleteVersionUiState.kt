package com.quare.bibleplanner.feature.deleteversion.presentation.model

internal sealed interface DeleteVersionUiState {
    data object Idle : DeleteVersionUiState

    data object Loading : DeleteVersionUiState
}
