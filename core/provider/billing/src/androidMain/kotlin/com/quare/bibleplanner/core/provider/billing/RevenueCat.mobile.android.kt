package com.quare.bibleplanner.core.provider.billing

import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases

internal actual fun getPlatformApiKey(): String = BuildKonfig.REVENUECAT_PLAY_STORE_API_KEY

internal actual fun configureLog() {
    Purchases.logLevel = LogLevel.DEBUG
}
