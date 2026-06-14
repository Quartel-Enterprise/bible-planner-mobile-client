package com.quare.bibleplanner.core.provider.connectivity.di

import com.quare.bibleplanner.core.provider.connectivity.NetworkConnectivityObserver
import org.koin.core.module.Module

/**
 * Provides the platform [NetworkConnectivityObserver] implementation. Each platform binds its own
 * (ConnectivityManager on Android, NWPathMonitor on iOS, a NetworkInterface poll on desktop).
 */
expect val connectivityModule: Module
