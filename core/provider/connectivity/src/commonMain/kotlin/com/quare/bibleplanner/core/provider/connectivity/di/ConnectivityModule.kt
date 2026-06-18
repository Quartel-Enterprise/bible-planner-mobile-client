package com.quare.bibleplanner.core.provider.connectivity.di

import com.quare.bibleplanner.core.provider.connectivity.NetworkConnectivityObserver
import com.quare.bibleplanner.core.provider.connectivity.domain.usecase.IsConnected
import com.quare.bibleplanner.core.provider.connectivity.domain.usecase.impl.IsConnectedUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val connectivityModule = module {
    includes(platformConnectivityModule)
    factoryOf(::IsConnectedUseCase).bind<IsConnected>()
}

/**
 * Provides the platform [NetworkConnectivityObserver] implementation. Each platform binds its own
 * (ConnectivityManager on Android, NWPathMonitor on iOS, a NetworkInterface poll on desktop).
 */
internal expect val platformConnectivityModule: Module
