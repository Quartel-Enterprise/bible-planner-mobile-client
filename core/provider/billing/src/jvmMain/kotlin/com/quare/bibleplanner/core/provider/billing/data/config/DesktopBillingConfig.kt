package com.quare.bibleplanner.core.provider.billing.data.config

internal data class DesktopBillingConfig(
    val apiKey: String,
    val purchaseLink: String,
) {
    val isEntitlementReadEnabled: Boolean
        get() = apiKey.isNotBlank()

    val isPurchaseEnabled: Boolean
        get() = isEntitlementReadEnabled && purchaseLink.isNotBlank()
}
