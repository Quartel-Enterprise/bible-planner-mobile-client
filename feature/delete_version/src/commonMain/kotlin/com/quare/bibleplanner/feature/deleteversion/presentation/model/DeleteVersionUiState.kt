package com.quare.bibleplanner.feature.deleteversion.presentation.model

sealed interface DeleteVersionUiState {
    data object Idle : DeleteVersionUiState

    data object Loading : DeleteVersionUiState
}
