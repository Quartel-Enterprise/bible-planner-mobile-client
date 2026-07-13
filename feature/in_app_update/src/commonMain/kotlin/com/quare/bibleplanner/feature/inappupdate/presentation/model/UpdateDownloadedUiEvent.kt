package com.quare.bibleplanner.feature.inappupdate.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface UpdateDownloadedUiEvent : UiEvent {
    data object OnRestartNowClick : UpdateDownloadedUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.UPDATE_INSTALL_STARTED,
            params = emptyMap(),
        )
    }

    data object OnLaterClick : UpdateDownloadedUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.UPDATE_INSTALL_POSTPONED,
            params = emptyMap(),
        )
    }
}
