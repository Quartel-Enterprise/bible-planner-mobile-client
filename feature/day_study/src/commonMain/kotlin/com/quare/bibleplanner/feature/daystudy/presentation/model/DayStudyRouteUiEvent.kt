package com.quare.bibleplanner.feature.daystudy.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface DayStudyRouteUiEvent : UiEvent {
    data object OnCardClick : DayStudyRouteUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            setOf(
                AnalyticsEventNames.DAY_STUDY_OPENED,
                AnalyticsEventNames.DAY_STUDY_GENERATION_FAILED,
                AnalyticsEventNames.DAY_STUDY_GENERATION_STARTED,
            ),
        )
    }
}
