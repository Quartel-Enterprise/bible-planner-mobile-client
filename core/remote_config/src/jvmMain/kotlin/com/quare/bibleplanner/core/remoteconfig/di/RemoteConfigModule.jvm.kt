package com.quare.bibleplanner.core.remoteconfig.di

import com.quare.bibleplanner.core.remoteconfig.DesktopRemoteConfigService
import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platFormRemoteConfigModule: Module = module {
    single { DesktopRemoteConfigService() }.bind<RemoteConfigService>()
}
