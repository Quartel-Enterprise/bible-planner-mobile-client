package com.quare.bibleplanner.core.provider.crashlytics

import com.quare.bibleplanner.core.provider.crashlytics.domain.service.CrashReporter

class DesktopCrashReporter : CrashReporter {
    override fun setCollectionEnabled(enabled: Boolean) = Unit

    override fun recordException(throwable: Throwable) = Unit
}
