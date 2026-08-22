package com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model

import androidx.navigation3.runtime.NavKey
import org.jetbrains.compose.resources.StringResource

sealed interface DayReadingCompleteUiAction {
    data object NavigateBack : DayReadingCompleteUiAction

    data class NavigateToRoute(
        val route: NavKey,
        val replace: Boolean,
    ) : DayReadingCompleteUiAction

    data class ShowSnackBar(
        val message: StringResource,
    ) : DayReadingCompleteUiAction
}
