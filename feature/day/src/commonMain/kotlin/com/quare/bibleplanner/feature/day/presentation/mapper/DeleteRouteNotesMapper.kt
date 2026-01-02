package com.quare.bibleplanner.feature.day.presentation.mapper

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.route.DeleteNotesRoute
import com.quare.bibleplanner.feature.day.presentation.model.DayUiAction

internal class DeleteRouteNotesMapper {
    fun map(
        hasNotes: Boolean,
        readingPlanType: ReadingPlanType,
        weekNumber: Int,
        dayNumber: Int,
    ): DayUiAction = if (hasNotes) {
        DayUiAction.NavigateToRoute(
            DeleteNotesRoute(
                readingPlanType = readingPlanType.name,
                week = weekNumber,
                day = dayNumber,
            ),
        )
    } else {
        DayUiAction.ShowNothingToDeleteSnackbar
    }
}
