package com.quare.bibleplanner.core.provider.crashlytics.di

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.quare.bibleplanner.core.provider.crashlytics.domain.service.AndroidCrashReporter
import com.quare.bibleplanner.core.provider.crashlytics.domain.service.CrashReporter
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformCrashlyticsModule: Module = module {
    single<FirebaseCrashlytics> { FirebaseCrashlytics.getInstance() }
    singleOf(::AndroidCrashReporter).bind<CrashReporter>()
}
