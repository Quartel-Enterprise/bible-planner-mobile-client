package com.quare.bibleplanner.feature.day.presentation.model

import org.jetbrains.compose.resources.StringResource

internal sealed interface DayUiAction {
    data class ShowSnackBar(
        val message: StringResource,
    ) : DayUiAction

    data class NavigateToRoute(
        val route: Any,
    ) : DayUiAction

    data object NavigateBack : DayUiAction

    data object ClearFocus : DayUiAction
}
