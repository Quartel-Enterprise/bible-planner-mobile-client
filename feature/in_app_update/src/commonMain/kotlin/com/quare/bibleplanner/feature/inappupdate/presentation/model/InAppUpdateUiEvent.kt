package com.quare.bibleplanner.feature.inappupdate.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface InAppUpdateUiEvent : UiEvent {
    data object OnUpdateClick : InAppUpdateUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.UPDATE_ACCEPTED,
        )
    }

    data object OnDismiss : InAppUpdateUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.UPDATE_DISMISSED,
        )
    }
}
