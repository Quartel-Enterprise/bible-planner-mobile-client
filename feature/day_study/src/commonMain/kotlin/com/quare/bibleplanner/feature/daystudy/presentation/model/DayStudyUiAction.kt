package com.quare.bibleplanner.feature.daystudy.presentation.model

import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource

internal sealed interface DayStudyUiAction {
    data object NavigateToPaywall : DayStudyUiAction

    data object NavigateToLoginWarning : DayStudyUiAction

    data class ShowSnackBar(
        val message: StringResource,
    ) : DayStudyUiAction

    data class ShowSnackBarPlural(
        val resource: PluralStringResource,
        val count: Int,
    ) : DayStudyUiAction
}
