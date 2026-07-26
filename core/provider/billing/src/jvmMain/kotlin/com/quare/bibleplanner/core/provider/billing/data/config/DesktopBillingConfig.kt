package com.quare.bibleplanner.core.provider.billing.data.config

internal data class DesktopBillingConfig(
    val productionApiKey: String,
    val productionPurchaseLink: String,
    val sandboxApiKey: String,
    val sandboxPurchaseLink: String,
    val isDebugBuild: Boolean,
) {
    val apiKey: String
        get() = if (isDebugBuild) sandboxApiKey else productionApiKey

    val purchaseLink: String
        get() = if (isDebugBuild) sandboxPurchaseLink else productionPurchaseLink

    val isEntitlementReadEnabled: Boolean
        get() = apiKey.isNotBlank()

    val isPurchaseEnabled: Boolean
        get() = isEntitlementReadEnabled && purchaseLink.isNotBlank()
}
