package com.quare.bibleplanner.core.provider.billing.data.config

internal fun desktopBillingConfig(
    apiKey: String = "rcb_test",
    purchaseLink: String = "https://pay.rev.cat/token",
): DesktopBillingConfig = DesktopBillingConfig(
    productionApiKey = apiKey,
    productionPurchaseLink = purchaseLink,
    sandboxApiKey = "rcb_sb_unused",
    sandboxPurchaseLink = "https://pay.rev.cat/sandbox/unused",
    isDebugBuild = false,
)
