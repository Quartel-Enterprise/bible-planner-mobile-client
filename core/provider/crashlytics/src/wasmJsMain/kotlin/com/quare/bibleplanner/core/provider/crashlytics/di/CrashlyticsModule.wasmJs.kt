package com.quare.bibleplanner.core.provider.crashlytics.di

import com.quare.bibleplanner.core.provider.crashlytics.WebCrashReporter
import com.quare.bibleplanner.core.provider.crashlytics.domain.service.CrashReporter
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformCrashlyticsModule: Module = module {
    single<CrashReporter> { WebCrashReporter() }
}
