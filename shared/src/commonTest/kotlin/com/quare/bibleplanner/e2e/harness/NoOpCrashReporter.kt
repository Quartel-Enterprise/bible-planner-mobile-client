package com.quare.bibleplanner.e2e.harness

import com.quare.bibleplanner.core.provider.crashlytics.domain.service.CrashReporter

internal class NoOpCrashReporter : CrashReporter {
    override fun setCollectionEnabled(enabled: Boolean) = Unit

    override fun recordException(throwable: Throwable) = Unit
}
