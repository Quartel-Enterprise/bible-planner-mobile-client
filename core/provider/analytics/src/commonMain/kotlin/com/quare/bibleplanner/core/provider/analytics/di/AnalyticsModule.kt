package com.quare.bibleplanner.core.provider.analytics.di

import com.quare.bibleplanner.core.provider.analytics.domain.mapper.NavRouteToDestinationMapper
import com.quare.bibleplanner.core.provider.analytics.domain.mapper.NavRouteToDestinationMapperImpl
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.ObserveTesterUserProperty
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackDestination
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.impl.ObserveTesterUserPropertyUseCase
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.impl.TrackDestinationUseCase
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.impl.TrackEventUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val analyticsModule = module {
    includes(platformAnalyticsModule)
    factoryOf(::ObserveTesterUserPropertyUseCase).bind<ObserveTesterUserProperty>()
    factoryOf(::TrackEventUseCase).bind<TrackEvent>()
    factoryOf(::NavRouteToDestinationMapperImpl).bind<NavRouteToDestinationMapper>()
    factoryOf(::TrackDestinationUseCase).bind<TrackDestination>()
}

internal expect val platformAnalyticsModule: Module
