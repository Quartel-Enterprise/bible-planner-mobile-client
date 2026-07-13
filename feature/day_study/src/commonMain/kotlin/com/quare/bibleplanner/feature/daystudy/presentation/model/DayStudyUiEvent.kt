package com.quare.bibleplanner.feature.daystudy.presentation.model

import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface DayStudyUiEvent : UiEvent {
    data class OnStart(
        val passages: List<PassageModel>,
        val dayRoute: DayNavRoute,
        val label: String,
    ) : DayStudyUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data object OnCardClick : DayStudyUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            setOf(
                AnalyticsEventNames.DAY_STUDY_CARD_CLICKED,
                AnalyticsEventNames.DAY_STUDY_OPENED,
                AnalyticsEventNames.DAY_STUDY_GENERATION_FAILED,
                AnalyticsEventNames.DAY_STUDY_GENERATION_STARTED,
            ),
        )
    }

    data object OnStudyDismiss : DayStudyUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.DAY_STUDY_DISMISSED,
            params = emptyMap(),
        )
    }
}
