package com.quare.bibleplanner.feature.daystudy.presentation.model

import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute

internal sealed interface DayStudyUiEvent {
    data class OnStart(
        val passages: List<PassageModel>,
        val dayRoute: DayNavRoute,
        val label: String,
    ) : DayStudyUiEvent

    data object OnCardClick : DayStudyUiEvent

    data object OnStudyDismiss : DayStudyUiEvent
}
