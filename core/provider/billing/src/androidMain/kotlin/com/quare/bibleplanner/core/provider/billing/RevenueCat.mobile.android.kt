package com.quare.bibleplanner.core.provider.billing

import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases

actual fun configureLog() {
    Purchases.logLevel = LogLevel.DEBUG
}
