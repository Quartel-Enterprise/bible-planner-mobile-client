package com.quare.bibleplanner.feature.deleteversion.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface DeleteVersionUiEvent : UiEvent {
    data object OnConfirmDelete : DeleteVersionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.BIBLE_VERSION_DELETED,
        )
    }

    data object OnCancel : DeleteVersionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.BIBLE_VERSION_DELETE_CANCELLED,
            params = emptyMap(),
        )
    }
}
