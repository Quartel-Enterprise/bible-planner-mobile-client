package com.quare.bibleplanner.feature.bibleversion.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface PendingBibleUpdatesUiEvent : UiEvent {
    data class OnToggleVersion(
        val id: String,
    ) : PendingBibleUpdatesUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.BIBLE_VERSION_UPDATE_PROMPT_VERSION_TOGGLED,
        )
    }

    data object OnUpdateClick : PendingBibleUpdatesUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.BIBLE_VERSION_UPDATE_PROMPT_UPDATE_CLICKED,
            params = emptyMap(),
        )
    }

    data object OnDismissClick : PendingBibleUpdatesUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.BIBLE_VERSION_UPDATE_PROMPT_DISMISSED,
            params = emptyMap(),
        )
    }
}
