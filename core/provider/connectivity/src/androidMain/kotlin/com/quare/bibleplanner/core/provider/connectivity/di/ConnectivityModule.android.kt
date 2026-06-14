package com.quare.bibleplanner.core.provider.connectivity.di

import android.content.Context
import android.net.ConnectivityManager
import com.quare.bibleplanner.core.provider.connectivity.AndroidNetworkConnectivityObserver
import com.quare.bibleplanner.core.provider.connectivity.NetworkConnectivityObserver
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val connectivityModule: Module = module {
    single { androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    singleOf(::AndroidNetworkConnectivityObserver).bind<NetworkConnectivityObserver>()
}
