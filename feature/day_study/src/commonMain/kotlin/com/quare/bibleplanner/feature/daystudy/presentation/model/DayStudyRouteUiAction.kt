package com.quare.bibleplanner.feature.daystudy.presentation.model

import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource

internal sealed interface DayStudyRouteUiAction {
    data class ShowSnackBar(
        val message: StringResource,
    ) : DayStudyRouteUiAction

    data class ShowSnackBarPlural(
        val resource: PluralStringResource,
        val count: Int,
    ) : DayStudyRouteUiAction
}
