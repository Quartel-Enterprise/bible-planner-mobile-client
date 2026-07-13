package com.quare.bibleplanner.feature.donation.pixqr.presentation

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface PixQrUiEvent : UiEvent {
    data object Dismiss : PixQrUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PIX_QR_DISMISSED,
            params = emptyMap(),
        )
    }

    data object Share : PixQrUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PIX_QR_SHARED,
            params = emptyMap(),
        )
    }
}
