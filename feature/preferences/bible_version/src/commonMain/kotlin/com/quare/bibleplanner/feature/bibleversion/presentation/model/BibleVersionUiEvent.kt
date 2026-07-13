package com.quare.bibleplanner.feature.bibleversion.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface BibleVersionUiEvent : UiEvent {
    data class OnDownload(
        val id: String,
    ) : BibleVersionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.BIBLE_VERSION_DOWNLOAD_STARTED,
            params = mapOf(
                AnalyticsParams.VERSION_ID to id,
                AnalyticsParams.IS_RESUME to false,
            ),
        )
    }

    data class OnPause(
        val id: String,
    ) : BibleVersionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.BIBLE_VERSION_DOWNLOAD_PAUSED,
            params = mapOf(AnalyticsParams.VERSION_ID to id),
        )
    }

    data class OnResume(
        val id: String,
    ) : BibleVersionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.BIBLE_VERSION_DOWNLOAD_STARTED,
            params = mapOf(
                AnalyticsParams.VERSION_ID to id,
                AnalyticsParams.IS_RESUME to true,
            ),
        )
    }

    data class OnDelete(
        val id: String,
    ) : BibleVersionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.BIBLE_VERSION_DELETE_CLICKED,
            params = mapOf(AnalyticsParams.VERSION_ID to id),
        )
    }

    data class OnSelect(
        val id: String,
    ) : BibleVersionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.BIBLE_VERSION_SELECTED,
        )
    }

    data object OnDismiss : BibleVersionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.BIBLE_VERSION_MANAGER_DISMISSED,
            params = emptyMap(),
        )
    }

    data object TryToDownloadBibleVersionsAgain : BibleVersionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.BIBLE_VERSION_DOWNLOAD_RETRY_CLICKED,
            params = emptyMap(),
        )
    }
}
