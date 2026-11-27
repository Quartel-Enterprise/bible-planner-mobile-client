package com.quare.bibleplanner.core.provider.crashlytics

import com.quare.bibleplanner.core.provider.crashlytics.domain.service.CrashReporter

internal class WebCrashReporter : CrashReporter {
    override fun setCollectionEnabled(enabled: Boolean) {}

    override fun recordException(throwable: Throwable) {}
}
