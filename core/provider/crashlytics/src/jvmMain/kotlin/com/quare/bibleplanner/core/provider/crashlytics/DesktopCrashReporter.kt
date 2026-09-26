package com.quare.bibleplanner.core.provider.crashlytics

import com.quare.bibleplanner.core.provider.crashlytics.domain.service.CrashReporter
import io.sentry.Sentry

internal class DesktopCrashReporter(
    private val sentryDsn: String,
    private val appVersion: String,
) : CrashReporter {
    override fun setCollectionEnabled(enabled: Boolean) {
        if (enabled) startSentry() else Sentry.close()
    }

    override fun recordException(throwable: Throwable) {
        Sentry.captureException(throwable)
    }

    private fun startSentry() {
        if (sentryDsn.isBlank()) return
        Sentry.init { options ->
            options.dsn = sentryDsn
            options.release = "$RELEASE_PACKAGE@$appVersion"
            options.isAttachServerName = false
        }
    }

    companion object {
        private const val RELEASE_PACKAGE = "com.quare.bibleplanner"
    }
}
