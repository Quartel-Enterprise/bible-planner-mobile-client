package com.quare.bibleplanner.feature.daystudy.presentation.model

import com.quare.bibleplanner.core.model.route.NavRoute
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource

internal sealed interface DayStudyRouteUiAction {
    data class NavigateToRoute(
        val route: NavRoute,
    ) : DayStudyRouteUiAction

    data class ShowSnackBar(
        val message: StringResource,
    ) : DayStudyRouteUiAction

    data class ShowSnackBarPlural(
        val resource: PluralStringResource,
        val count: Int,
    ) : DayStudyRouteUiAction
}
