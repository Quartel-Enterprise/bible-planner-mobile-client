package com.quare.bibleplanner.core.provider.connectivity

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

// Why: tracks networks from the event payload because re-querying the active network during
// teardown can still report the dying network as connected.
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
