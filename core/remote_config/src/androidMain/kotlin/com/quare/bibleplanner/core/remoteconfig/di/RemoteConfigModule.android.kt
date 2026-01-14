package com.quare.bibleplanner.core.remoteconfig.di

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.quare.bibleplanner.core.remoteconfig.domain.service.AndroidRemoteConfigService
import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platFormRemoteConfigModule: Module = module {
    single<FirebaseRemoteConfig> {
        FirebaseRemoteConfig.getInstance()
    }
    singleOf(::AndroidRemoteConfigService).bind<RemoteConfigService>()
}
