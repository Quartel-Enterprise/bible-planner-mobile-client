package com.quare.bibleplanner.feature.daystudy.presentation.model

import com.quare.bibleplanner.core.model.route.NavRoute

internal sealed interface DayStudyBackgroundGenerationUiAction {
    data class NavigateToRoute(
        val route: NavRoute,
    ) : DayStudyBackgroundGenerationUiAction
}
