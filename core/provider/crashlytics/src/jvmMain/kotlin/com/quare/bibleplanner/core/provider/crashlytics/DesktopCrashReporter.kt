package com.quare.bibleplanner.core.provider.crashlytics

import com.quare.bibleplanner.core.provider.crashlytics.domain.service.CrashReporter
import com.quare.bibleplanner.core.provider.crashlytics.generated.CrashlyticsBuildKonfig
import io.sentry.Sentry

internal class DesktopCrashReporter : CrashReporter {
    override fun setCollectionEnabled(enabled: Boolean) {
        if (enabled) startSentry() else Sentry.close()
    }

    override fun recordException(throwable: Throwable) {
        Sentry.captureException(throwable)
    }

    private fun startSentry() {
        val dsn = CrashlyticsBuildKonfig.SENTRY_DSN
        if (dsn.isBlank()) return
        Sentry.init { options ->
            options.dsn = dsn
            options.release = "$RELEASE_PACKAGE@${CrashlyticsBuildKonfig.APP_VERSION}"
            options.isAttachServerName = false
        }
    }

    companion object {
        private const val RELEASE_PACKAGE = "com.quare.bibleplanner"
    }
}
