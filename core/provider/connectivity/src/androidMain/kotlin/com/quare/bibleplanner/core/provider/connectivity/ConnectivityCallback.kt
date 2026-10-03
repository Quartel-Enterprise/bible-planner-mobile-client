package com.quare.bibleplanner.core.provider.connectivity

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

internal class ConnectivityCallback(
    private val onConnectivityChange: (isConnected: Boolean) -> Unit,
) : ConnectivityManager.NetworkCallback() {
    private val connectedNetworks = mutableSetOf<Network>()

    override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities,
    ) {
        if (networkCapabilities.hasInternet()) connectedNetworks += network else connectedNetworks -= network
        onConnectivityChange(connectedNetworks.isNotEmpty())
    }

    override fun onLost(network: Network) {
        connectedNetworks -= network
        onConnectivityChange(connectedNetworks.isNotEmpty())
    }
}
