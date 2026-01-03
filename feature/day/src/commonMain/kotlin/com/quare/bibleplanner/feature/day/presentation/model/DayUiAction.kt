package com.quare.bibleplanner.feature.day.presentation.model

internal sealed interface DayUiAction {
    data object ShowNothingToDeleteSnackbar : DayUiAction

    data class NavigateToRoute(
        val route: Any,
    ) : DayUiAction

    data object NavigateBack : DayUiAction

    data object ClearFocus : DayUiAction
}
