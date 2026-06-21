package com.quare.bibleplanner.core.provider.crashlytics

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.provider.crashlytics.domain.service.CrashReporter

fun CrashReporter.configure(isDebug: Boolean) {
    setCollectionEnabled(enabled = !isDebug)
    if (!isDebug) {
        Logger.addLogWriter(CrashReporterLogWriter(this))
    }
}
