package com.quare.bibleplanner.core.provider.analytics.di

import com.quare.bibleplanner.core.provider.analytics.domain.usecase.ObserveTesterUserProperty
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.impl.ObserveTesterUserPropertyUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val analyticsModule = module {
    includes(platformAnalyticsModule)
    factoryOf(::ObserveTesterUserPropertyUseCase).bind<ObserveTesterUserProperty>()
}

internal expect val platformAnalyticsModule: Module
