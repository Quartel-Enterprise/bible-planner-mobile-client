package com.quare.bibleplanner.core.provider.crashlytics

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import com.quare.bibleplanner.core.provider.crashlytics.domain.service.CrashReporter

internal class CrashReporterLogWriter(
    private val crashReporter: CrashReporter,
) : LogWriter() {
    override fun isLoggable(
        tag: String,
        severity: Severity,
    ): Boolean = severity >= Severity.Error

    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?,
    ) {
        throwable?.let(crashReporter::recordException)
    }
}
