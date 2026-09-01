package com.quare.bibleplanner.feature.studysuggestion.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface StudySuggestionUiEvent : UiEvent {
    data class OnToggleClick(
        val isNewValueOn: Boolean,
    ) : StudySuggestionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.STUDY_SUGGESTION_TOGGLED,
            params = mapOf(
                AnalyticsParams.IS_ENABLED to isNewValueOn,
                AnalyticsParams.SOURCE to SOURCE_SETTINGS,
            ),
        )
    }

    data class OnModeClick(
        val mode: StudySuggestionMode,
    ) : StudySuggestionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.STUDY_SUGGESTION_MODE_CHANGED,
            params = mapOf(AnalyticsParams.MODE to mode.name.lowercase()),
        )
    }

    data object OnBlockedModeClick : StudySuggestionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.STUDY_SUGGESTION_MODE_BLOCKED_CLICKED,
            params = emptyMap(),
        )
    }

    data object OnDismiss : StudySuggestionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.STUDY_SUGGESTION_DISMISSED,
            params = emptyMap(),
        )
    }

    data class SyncToggleClicked(
        val isNewValueOn: Boolean,
    ) : StudySuggestionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.SETTING_SYNC_TOGGLED,
            params = mapOf(
                AnalyticsParams.SETTING to SYNC_SETTING,
                AnalyticsParams.IS_ENABLED to isNewValueOn,
            ),
        )
    }

    data object SyncToggleBlockedClicked : StudySuggestionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.SYNC_TOGGLE_BLOCKED_CLICKED,
            params = mapOf(AnalyticsParams.SOURCE to SYNC_BLOCKED_SOURCE),
        )
    }

    private companion object {
        const val SOURCE_SETTINGS = "settings"
        const val SYNC_SETTING = "study_suggestion"
        const val SYNC_BLOCKED_SOURCE = "study_suggestion"
    }
}
