package com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface DayReadingCompleteBannerUiEvent : UiEvent {
    data class OnCtaClick(
        val readingLabel: String,
    ) : DayReadingCompleteBannerUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.DAY_READING_COMPLETE_BANNER_CTA_CLICKED,
        )
    }

    data object OnDismissClick : DayReadingCompleteBannerUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.DAY_READING_COMPLETE_BANNER_DISMISSED,
            params = emptyMap(),
        )
    }
}
