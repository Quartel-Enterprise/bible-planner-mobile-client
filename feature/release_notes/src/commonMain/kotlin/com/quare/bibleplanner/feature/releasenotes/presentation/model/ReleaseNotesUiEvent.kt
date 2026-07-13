package com.quare.bibleplanner.feature.releasenotes.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface ReleaseNotesUiEvent : UiEvent {
    data class OnTabSelected(
        val tab: ReleaseNotesTab,
    ) : ReleaseNotesUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.RELEASE_NOTES_TAB_SELECTED,
            params = mapOf(AnalyticsParams.TAB to tab.name.lowercase()),
        )
    }

    data object OnBackClicked : ReleaseNotesUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.RELEASE_NOTES_BACK_CLICKED,
            params = emptyMap(),
        )
    }

    data object OnGithubAllReleasesClicked : ReleaseNotesUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.GITHUB_RELEASE_OPENED,
            params = emptyMap(),
        )
    }

    data class OnGithubVersionClicked(
        val version: String,
    ) : ReleaseNotesUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.GITHUB_RELEASE_OPENED,
            params = mapOf(AnalyticsParams.VERSION to version),
        )
    }
}
