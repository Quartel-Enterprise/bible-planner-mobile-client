package com.quare.bibleplanner.feature.day.presentation.model

import org.jetbrains.compose.resources.StringResource

internal sealed interface DayUiAction {
    data class ShowSnackBar(
        val message: StringResource,
    ) : DayUiAction

    data class ShowSnackBarText(
        val message: String,
    ) : DayUiAction

    data object ClearFocus : DayUiAction
}
