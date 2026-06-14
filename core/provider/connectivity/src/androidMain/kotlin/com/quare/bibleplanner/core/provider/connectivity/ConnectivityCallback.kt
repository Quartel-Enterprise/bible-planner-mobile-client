package com.quare.bibleplanner.core.provider.connectivity

import android.Manifest
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission

/**
 * Bridges [ConnectivityManager] network events to a simple is-online callback. On loss it re-checks
 * the active network (another network may still provide connectivity).
 */
internal class ConnectivityCallback(
    private val connectivityManager: ConnectivityManager,
    private val onConnectivityChange: (isConnected: Boolean) -> Unit,
) : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
        onConnectivityChange(true)
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override fun onLost(network: Network) {
        onConnectivityChange(connectivityManager.isCurrentlyConnected())
    }

    override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities,
    ) {
        onConnectivityChange(networkCapabilities.hasInternet())
    }
}
