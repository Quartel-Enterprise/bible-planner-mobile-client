package com.quare.bibleplanner.feature.daystudy.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationJob
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface DayStudyBackgroundGenerationUiEvent : UiEvent {
    override val analytics: EventAnalytics get() = EventAnalytics.NotTracked

    data class OnOpenClick(
        val job: DayStudyGenerationJob,
    ) : DayStudyBackgroundGenerationUiEvent

    data class OnDismissClick(
        val keys: List<String>,
    ) : DayStudyBackgroundGenerationUiEvent
}
