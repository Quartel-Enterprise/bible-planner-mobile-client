package com.quare.bibleplanner.core.provider.connectivity

import android.Manifest
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

internal class AndroidNetworkConnectivityObserver(
    private val connectivityManager: ConnectivityManager,
) : NetworkConnectivityObserver {
    private val networkRequest = NetworkRequest
        .Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override fun observe(): Flow<Boolean> = callbackFlow {
        val callback = ConnectivityCallback(
            connectivityManager = connectivityManager,
            onConnectivityChange = ::trySend,
        )
        trySend(connectivityManager.isCurrentlyConnected())
        connectivityManager.registerNetworkCallback(networkRequest, callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
