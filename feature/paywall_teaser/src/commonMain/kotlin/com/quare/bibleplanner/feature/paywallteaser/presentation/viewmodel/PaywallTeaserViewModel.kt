package com.quare.bibleplanner.feature.paywallteaser.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.route.PaywallTeaserNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.paywallteaser.presentation.model.PaywallTeaserUiAction
import com.quare.bibleplanner.feature.paywallteaser.presentation.model.PaywallTeaserUiEvent
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal class PaywallTeaserViewModel(
    route: PaywallTeaserNavRoute,
    trackEvent: TrackEvent,
) : TrackedViewModel<PaywallTeaserUiEvent>(trackEvent) {
    val reason = route.reason

    private val _uiAction = MutableSharedFlow<PaywallTeaserUiAction>()
    val uiAction: SharedFlow<PaywallTeaserUiAction> = _uiAction

    override fun handleEvent(event: PaywallTeaserUiEvent) {
        trackEvent(
            name = when (event) {
                PaywallTeaserUiEvent.OnSubscribeClick -> AnalyticsEventNames.PAYWALL_TEASER_SUBSCRIBE_CLICKED
                PaywallTeaserUiEvent.OnDismiss -> AnalyticsEventNames.PAYWALL_TEASER_DISMISSED
            },
            params = mapOf(AnalyticsParams.REASON to reason.key),
        )
        val action = when (event) {
            PaywallTeaserUiEvent.OnSubscribeClick -> PaywallTeaserUiAction.NavigateToPaywall
            PaywallTeaserUiEvent.OnDismiss -> PaywallTeaserUiAction.NavigateBack
        }
        viewModelScope.launch { _uiAction.emit(action) }
    }
}
