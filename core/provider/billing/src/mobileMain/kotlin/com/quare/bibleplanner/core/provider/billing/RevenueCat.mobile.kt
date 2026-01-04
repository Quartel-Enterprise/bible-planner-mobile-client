package com.quare.bibleplanner.core.provider.billing

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure

fun configureRevenueCat(isDebug: Boolean) {
    Purchases.configure(
        apiKey = if (isDebug) {
            BuildKonfig.REVENUECAT_TEST_API_KEY
        } else {
            getPlatformApiKey()
        }
    )
    configureLog()
}

internal expect fun getPlatformApiKey(): String

internal expect fun configureLog()
