package com.quare.bibleplanner.feature.read.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

/**
 * Represents actions that can be performed by the user on the Read screen.
 */
sealed interface ReadUiEvent : UiEvent {
    /**
     * Event triggered when the user wants to navigate back.
     */
    data object OnArrowBackClick : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.READ_BACK_CLICKED,
            params = emptyMap(),
        )
    }

    /**
     * Event triggered to retry loading the chapter content if it failed.
     */
    data object OnRetryClick : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.READ_RETRY_CLICKED,
            params = emptyMap(),
        )
    }

    /**
     * Event triggered to toggle the read status of the current chapter.
     */
    data object ToggleReadStatus : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.CHAPTER_READ_TOGGLED,
        )
    }

    /**
     * Event triggered when the user wants to change the Bible version.
     */
    data object ManageBibleVersions : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.BIBLE_VERSION_MANAGE_CLICKED,
            params = mapOf(AnalyticsParams.SOURCE to "read_screen"),
        )
    }

    data class OnNavigationSuggestionClick(
        val suggestion: ReadNavigationSuggestionModel,
    ) : ReadUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.READING_SUGGESTION_CLICKED,
        )
    }
}
