package com.quare.bibleplanner.core.remoteconfig.di

import com.quare.bibleplanner.core.remoteconfig.data.datasource.RemoteConfigProxyClient
import com.quare.bibleplanner.core.remoteconfig.data.datasource.SupabaseRemoteConfigProxyClient
import com.quare.bibleplanner.core.remoteconfig.domain.service.DesktopRemoteConfigService
import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platFormRemoteConfigModule: Module = module {
    singleOf(::SupabaseRemoteConfigProxyClient).bind<RemoteConfigProxyClient>()
    single {
        DesktopRemoteConfigService(
            proxyClient = get(),
            coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        )
    }.bind<RemoteConfigDataSource>()
}
