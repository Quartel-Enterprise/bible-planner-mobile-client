package com.quare.bibleplanner.feature.congrats.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface CongratsUiEvent : UiEvent {
    data object OnDismiss : CongratsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CONGRATS_DISMISSED,
            params = emptyMap(),
        )
    }

    data object OnStartExploring : CongratsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CONGRATS_EXPLORE_CLICKED,
            params = emptyMap(),
        )
    }
}
