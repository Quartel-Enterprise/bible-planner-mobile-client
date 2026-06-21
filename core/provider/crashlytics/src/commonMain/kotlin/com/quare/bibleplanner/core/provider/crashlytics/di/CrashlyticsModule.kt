package com.quare.bibleplanner.core.provider.crashlytics.di

import org.koin.core.module.Module
import org.koin.dsl.module

val crashlyticsModule = module {
    includes(platformCrashlyticsModule)
}

internal expect val platformCrashlyticsModule: Module
