package com.quare.bibleplanner.core.remoteconfig.di

import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetBooleanRemoteConfig
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetIntRemoteConfig
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetStringRemoteConfig
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl.GetBooleanRemoteConfigUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl.GetIntRemoteConfigUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl.GetStringRemoteConfigUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.login.IsLoginVisible
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.login.usecase.IsLoginVisibleUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.GetWebAppUrl
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.IsMoreWebAppEnabled
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.impl.GetWebAppUrlUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.impl.IsMoreWebAppEnabledUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val remoteConfigModule = module {
    includes(platFormRemoteConfigModule)
    factoryOf(::GetIntRemoteConfigUseCase).bind<GetIntRemoteConfig>()
    factoryOf(::GetBooleanRemoteConfigUseCase).bind<GetBooleanRemoteConfig>()
    factoryOf(::GetStringRemoteConfigUseCase).bind<GetStringRemoteConfig>()
    factoryOf(::IsMoreWebAppEnabledUseCase).bind<IsMoreWebAppEnabled>()
    factoryOf(::GetWebAppUrlUseCase).bind<GetWebAppUrl>()
    factoryOf(::IsLoginVisibleUseCase).bind<IsLoginVisible>()
}

internal expect val platFormRemoteConfigModule: Module
