package com.quare.bibleplanner.core.provider.connectivity.di

import com.quare.bibleplanner.core.provider.connectivity.IosNetworkConnectivityObserver
import com.quare.bibleplanner.core.provider.connectivity.NetworkConnectivityObserver
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val connectivityModule: Module = module {
    singleOf(::IosNetworkConnectivityObserver).bind<NetworkConnectivityObserver>()
}
