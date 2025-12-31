package com.quare.bibleplanner.feature.day.presentation.model

internal sealed interface DayUiAction {
    data object ShowNothingToDeleteSnackbar : DayUiAction

    data object NavigateBack : DayUiAction
}
