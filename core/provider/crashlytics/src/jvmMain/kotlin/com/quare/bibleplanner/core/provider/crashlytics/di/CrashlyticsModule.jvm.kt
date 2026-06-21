package com.quare.bibleplanner.core.provider.crashlytics.di

import com.quare.bibleplanner.core.provider.crashlytics.DesktopCrashReporter
import com.quare.bibleplanner.core.provider.crashlytics.domain.service.CrashReporter
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformCrashlyticsModule: Module = module {
    single { DesktopCrashReporter() }.bind<CrashReporter>()
}
