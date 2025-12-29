package com.quare.bibleplanner.feature.editplanstartdate.presentation.model

internal sealed interface EditPlanStartDateUiState {
    data object Loading : EditPlanStartDateUiState

    data class Loaded(
        val initialTimestamp: Long,
    ) : EditPlanStartDateUiState
}

