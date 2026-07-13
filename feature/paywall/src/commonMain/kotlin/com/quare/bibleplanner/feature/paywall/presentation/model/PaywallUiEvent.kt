package com.quare.bibleplanner.feature.paywall.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.feature.paywall.domain.model.SubscriptionPlanType
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface PaywallUiEvent : UiEvent {
    data object OnBackClick : PaywallUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.PAYWALL_DISMISSED,
        )
    }

    data class OnPlanSelected(
        val planType: SubscriptionPlanType,
    ) : PaywallUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.PAYWALL_PLAN_SELECTED,
        )
    }

    data object OnStartProJourneyClick : PaywallUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            setOf(
                AnalyticsEventNames.PURCHASE_STARTED,
                AnalyticsEventNames.PURCHASE_COMPLETED,
                AnalyticsEventNames.PURCHASE_FAILED,
            ),
        )
    }

    data object OnRestorePurchases : PaywallUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            setOf(
                AnalyticsEventNames.RESTORE_COMPLETED,
                AnalyticsEventNames.RESTORE_FAILED,
            ),
        )
    }
}
