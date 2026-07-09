package com.quare.bibleplanner.feature.day.presentation.model

import androidx.navigation3.runtime.NavKey
import org.jetbrains.compose.resources.StringResource

internal sealed interface DayUiAction {
    data class ShowSnackBar(
        val message: StringResource,
    ) : DayUiAction

    data class ShowSnackBarText(
        val message: String,
    ) : DayUiAction

    data class NavigateToRoute(
        val route: NavKey,
    ) : DayUiAction

    data object NavigateBack : DayUiAction

    data object ClearFocus : DayUiAction
}
