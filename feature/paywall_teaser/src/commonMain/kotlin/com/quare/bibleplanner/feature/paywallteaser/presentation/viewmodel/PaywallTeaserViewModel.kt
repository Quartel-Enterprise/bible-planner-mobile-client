package com.quare.bibleplanner.feature.paywallteaser.presentation.viewmodel

import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserReason
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.paywallteaser.presentation.model.PaywallTeaserUiEvent
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel

internal class PaywallTeaserViewModel(
    route: PaywallTeaserNavRoute,
    private val navigator: Navigator,
    trackEvent: TrackEvent,
) : TrackedViewModel<PaywallTeaserUiEvent>(trackEvent) {
    val reason = route.reason

    override fun handleEvent(event: PaywallTeaserUiEvent) {
        trackEvent(
            name = when (event) {
                PaywallTeaserUiEvent.OnSubscribeClick -> AnalyticsEventNames.PAYWALL_TEASER_SUBSCRIBE_CLICKED
                PaywallTeaserUiEvent.OnDismiss -> AnalyticsEventNames.PAYWALL_TEASER_DISMISSED
            },
            params = mapOf(AnalyticsParams.REASON to reason.key),
        )
        when (event) {
            PaywallTeaserUiEvent.OnSubscribeClick -> navigator.navigateReplacingTop(
                PaywallNavRoute(reason.toPaywallEntrySource()),
            )

            PaywallTeaserUiEvent.OnDismiss -> navigator.navigateBack()
        }
    }
}

private fun PaywallTeaserReason.toPaywallEntrySource(): PaywallEntrySource = when (this) {
    PaywallTeaserReason.HIGHLIGHT_CUSTOM_COLOR -> PaywallEntrySource.HIGHLIGHT_CUSTOM_COLOR
}
