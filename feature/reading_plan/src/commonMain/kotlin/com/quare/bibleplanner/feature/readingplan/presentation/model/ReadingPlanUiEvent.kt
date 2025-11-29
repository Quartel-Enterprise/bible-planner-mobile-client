package com.quare.bibleplanner.feature.readingplan.presentation.model

import com.quare.bibleplanner.core.model.plan.ReadingPlanType

internal sealed interface ReadingPlanUiEvent {
    data class OnPlanClick(
        val type: ReadingPlanType,
    ) : ReadingPlanUiEvent

    data class OnWeekExpandClick(
        val weekNumber: Int,
    ) : ReadingPlanUiEvent
}
