package com.quare.bibleplanner.feature.readingplan.presentation.model

internal sealed interface ReadingPlanUiState {
    data class Loaded(val data: ReadingPlanUiModel) : ReadingPlanUiState
    data object Loading : ReadingPlanUiState
}
