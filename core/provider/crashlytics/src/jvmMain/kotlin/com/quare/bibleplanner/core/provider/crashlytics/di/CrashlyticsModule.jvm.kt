package com.quare.bibleplanner.core.provider.crashlytics.di

import com.quare.bibleplanner.core.provider.crashlytics.DesktopCrashReporter
import com.quare.bibleplanner.core.provider.crashlytics.domain.service.CrashReporter
import com.quare.bibleplanner.core.provider.crashlytics.generated.CrashlyticsBuildKonfig
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformCrashlyticsModule: Module = module {
    single<CrashReporter> {
        DesktopCrashReporter(
            sentryDsn = CrashlyticsBuildKonfig.SENTRY_DSN,
            appVersion = CrashlyticsBuildKonfig.APP_VERSION,
        )
    }
}
