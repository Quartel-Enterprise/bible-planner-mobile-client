package com.quare.bibleplanner.core.provider.billing.data.config

import com.quare.bibleplanner.core.provider.billing.BuildKonfig

internal class DesktopBillingConfigProvider(
    private val isDebugBuild: Boolean,
) {
    fun getConfig(): DesktopBillingConfig {
        val apiKey = if (isDebugBuild) {
            BuildKonfig.REVENUECAT_WEB_BILLING_SANDBOX_API_KEY
        } else {
            BuildKonfig.REVENUECAT_WEB_BILLING_API_KEY
        }
        val purchaseLink = if (isDebugBuild) {
            BuildKonfig.REVENUECAT_WEB_PURCHASE_LINK_SANDBOX
        } else {
            BuildKonfig.REVENUECAT_WEB_PURCHASE_LINK
        }
        return DesktopBillingConfig(
            apiKey = apiKey,
            purchaseLink = purchaseLink,
        )
    }
}
