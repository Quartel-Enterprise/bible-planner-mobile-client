package com.quare.bibleplanner.feature.paywallteaser.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface PaywallTeaserUiEvent : UiEvent {
    data object OnSubscribeClick : PaywallTeaserUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.PAYWALL_TEASER_SUBSCRIBE_CLICKED,
        )
    }

    data object OnDismiss : PaywallTeaserUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.PAYWALL_TEASER_DISMISSED,
        )
    }
}
