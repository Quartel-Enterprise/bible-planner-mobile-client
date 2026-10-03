package com.quare.bibleplanner.core.provider.connectivity.di

import com.quare.bibleplanner.core.provider.connectivity.NetworkConnectivityObserver
import com.quare.bibleplanner.core.provider.connectivity.WebNetworkConnectivityObserver
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.time.Duration.Companion.seconds

internal actual val platformConnectivityModule: Module = module {
    single<NetworkConnectivityObserver> { WebNetworkConnectivityObserver(pollInterval = 3.seconds) }
}
