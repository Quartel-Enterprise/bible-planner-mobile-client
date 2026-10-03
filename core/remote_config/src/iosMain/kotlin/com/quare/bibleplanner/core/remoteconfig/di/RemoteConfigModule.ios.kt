package com.quare.bibleplanner.core.remoteconfig.di

import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platFormRemoteConfigModule: Module = module {
    // Why: empty on purpose; the Swift IosRemoteConfigService is bound as
    // RemoteConfigDataSource when the iOS app starts Koin.
}
