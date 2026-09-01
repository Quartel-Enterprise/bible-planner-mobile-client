package com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface DayReadingCompleteUiEvent : UiEvent {
    data class OnCtaClick(
        val readingLabel: String,
    ) : DayReadingCompleteUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.DAY_READING_COMPLETE_CTA_CLICKED,
        )
    }

    data object OnDismiss : DayReadingCompleteUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.DAY_READING_COMPLETE_DISMISSED,
            params = emptyMap(),
        )
    }

    data object OnNeverShowAgainClick : DayReadingCompleteUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.STUDY_SUGGESTION_TOGGLED,
            params = mapOf(
                AnalyticsParams.IS_ENABLED to false,
                AnalyticsParams.SOURCE to "day_reading_complete",
            ),
        )
    }
}
