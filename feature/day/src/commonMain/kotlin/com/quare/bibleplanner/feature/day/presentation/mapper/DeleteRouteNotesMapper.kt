package com.quare.bibleplanner.feature.day.presentation.mapper

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.route.DeleteNotesRoute

internal class DeleteRouteNotesMapper {
    fun map(
        hasNotes: Boolean,
        readingPlanType: ReadingPlanType,
        weekNumber: Int,
        dayNumber: Int,
    ): DeleteNotesRoute? = if (hasNotes) {
        DeleteNotesRoute(
            readingPlanType = readingPlanType.name,
            week = weekNumber,
            day = dayNumber,
        )
    } else {
        null
    }
}
