package com.quare.bibleplanner.core.remoteconfig.di

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.GetBooleanRemoteConfig
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.GetIntRemoteConfig
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.impl.GetBooleanRemoteConfigUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.impl.GetIntRemoteConfigUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val remoteConfigModule = module {
    includes(platFormRemoteConfigModule)
    factoryOf(::GetIntRemoteConfigUseCase).bind<GetIntRemoteConfig>()
    factoryOf(::GetBooleanRemoteConfigUseCase).bind<GetBooleanRemoteConfig>()
}

internal expect val platFormRemoteConfigModule: Module
