package com.quare.bibleplanner.feature.day.presentation.model

sealed interface DayUiAction {
    data class ShowSnackBar(
        val message: DayMessage,
    ) : DayUiAction

    data class ShowSnackBarText(
        val message: String,
    ) : DayUiAction

    data object ClearFocus : DayUiAction
}
