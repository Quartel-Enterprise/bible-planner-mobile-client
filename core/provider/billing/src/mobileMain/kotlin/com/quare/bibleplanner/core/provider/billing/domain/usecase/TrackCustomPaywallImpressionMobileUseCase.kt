package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.platform.domain.usecase.IsDebugBuild
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.CustomPaywallImpressionParams

internal class TrackCustomPaywallImpressionMobileUseCase(
    private val purchases: Purchases,
    private val isDebugBuild: IsDebugBuild,
) : TrackCustomPaywallImpression {
    override fun invoke() {
        if (isDebugBuild()) return
        purchases.trackCustomPaywallImpression(CustomPaywallImpressionParams(paywallId = PAYWALL_ID))
    }

    private companion object {
        const val PAYWALL_ID = "main_paywall"
    }
}
