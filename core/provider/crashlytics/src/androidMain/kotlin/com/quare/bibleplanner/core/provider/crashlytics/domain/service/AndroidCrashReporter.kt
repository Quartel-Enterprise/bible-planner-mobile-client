package com.quare.bibleplanner.core.provider.crashlytics.domain.service

import com.google.firebase.crashlytics.FirebaseCrashlytics

internal class AndroidCrashReporter(
    private val crashlytics: FirebaseCrashlytics,
) : CrashReporter {
    override fun setCollectionEnabled(enabled: Boolean) {
        crashlytics.isCrashlyticsCollectionEnabled = enabled
    }

    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }
}
