package com.quare.bibleplanner.feature.deleteprogress.presentation.model

sealed interface DeleteAllProgressUiState {
    data object Idle : DeleteAllProgressUiState

    data object Loading : DeleteAllProgressUiState

    data object Success : DeleteAllProgressUiState
}
