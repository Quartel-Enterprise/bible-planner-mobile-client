package com.quare.bibleplanner.core.provider.analytics.di

import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.quare.bibleplanner.core.provider.analytics.domain.service.AnalyticsService
import com.quare.bibleplanner.core.provider.analytics.domain.service.AndroidAnalyticsService
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformAnalyticsModule: Module = module {
    single<FirebaseAnalytics> { Firebase.analytics }
    singleOf(::AndroidAnalyticsService).bind<AnalyticsService>()
}
