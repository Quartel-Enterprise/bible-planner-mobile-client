package com.quare.bibleplanner.core.provider.billing

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure

fun configureRevenueCat() {
    Purchases.configure(apiKey = BuildKonfig.REVENUECAT_API_KEY)
    configureLog()
}

internal expect fun configureLog()
