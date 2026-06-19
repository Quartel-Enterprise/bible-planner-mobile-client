package com.quare.bibleplanner.core.provider.analytics.di

import com.quare.bibleplanner.core.provider.analytics.DesktopAnalyticsService
import com.quare.bibleplanner.core.provider.analytics.domain.service.AnalyticsService
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformAnalyticsModule: Module = module {
    single { DesktopAnalyticsService() }.bind<AnalyticsService>()
}
