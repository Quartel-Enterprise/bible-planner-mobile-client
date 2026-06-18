package com.quare.bibleplanner.core.provider.connectivity.di

import com.quare.bibleplanner.core.provider.connectivity.DesktopNetworkConnectivityObserver
import com.quare.bibleplanner.core.provider.connectivity.NetworkConnectivityObserver
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.time.Duration.Companion.seconds

internal actual val platformConnectivityModule: Module = module {
    single<NetworkConnectivityObserver> { DesktopNetworkConnectivityObserver(pollInterval = 3.seconds) }
}
