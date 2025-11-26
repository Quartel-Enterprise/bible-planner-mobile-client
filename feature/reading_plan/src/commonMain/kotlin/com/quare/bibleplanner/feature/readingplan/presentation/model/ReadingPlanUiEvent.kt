package com.quare.bibleplanner.feature.readingplan.presentation.model

import com.quare.bibleplanner.feature.readingplan.domain.model.ReadingPlanType

internal sealed interface ReadingPlanUiEvent {
    data class OnPlanClick(
        val type: ReadingPlanType,
    ) : ReadingPlanUiEvent
}
