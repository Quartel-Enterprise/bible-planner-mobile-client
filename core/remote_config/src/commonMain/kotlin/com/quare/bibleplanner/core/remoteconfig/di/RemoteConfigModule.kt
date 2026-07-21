package com.quare.bibleplanner.core.remoteconfig.di

import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService
import com.quare.bibleplanner.core.remoteconfig.domain.service.impl.RemoteConfigServiceImpl
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetBooleanRemoteConfig
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetIntRemoteConfig
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetStringRemoteConfig
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.ObserveBooleanRemoteConfig
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.ObserveIntRemoteConfig
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.ObserveStringRemoteConfig
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl.GetBooleanRemoteConfigUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl.GetIntRemoteConfigUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl.GetStringRemoteConfigUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl.ObserveBooleanRemoteConfigUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl.ObserveIntRemoteConfigUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.impl.ObserveStringRemoteConfigUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.GetWebAppUrl
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.ObserveProfileWebAppEnabled
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.impl.GetWebAppUrlUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.impl.ObserveProfileWebAppEnabledUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val remoteConfigModule = module {
    includes(platFormRemoteConfigModule)
    singleOf(::RemoteConfigServiceImpl).bind<RemoteConfigService>()
    factoryOf(::GetIntRemoteConfigUseCase).bind<GetIntRemoteConfig>()
    factoryOf(::GetBooleanRemoteConfigUseCase).bind<GetBooleanRemoteConfig>()
    factoryOf(::GetStringRemoteConfigUseCase).bind<GetStringRemoteConfig>()
    factoryOf(::ObserveBooleanRemoteConfigUseCase).bind<ObserveBooleanRemoteConfig>()
    factoryOf(::ObserveIntRemoteConfigUseCase).bind<ObserveIntRemoteConfig>()
    factoryOf(::ObserveStringRemoteConfigUseCase).bind<ObserveStringRemoteConfig>()
    factoryOf(::ObserveProfileWebAppEnabledUseCase).bind<ObserveProfileWebAppEnabled>()
    factoryOf(::GetWebAppUrlUseCase).bind<GetWebAppUrl>()
}

internal expect val platFormRemoteConfigModule: Module
