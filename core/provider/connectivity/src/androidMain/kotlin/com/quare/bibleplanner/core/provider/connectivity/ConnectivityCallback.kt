package com.quare.bibleplanner.core.provider.connectivity

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

/**
 * Bridges [ConnectivityManager] network events to a simple is-online callback by tracking the set
 * of networks that currently advertise validated internet. Working from the event's own payload
 * avoids re-querying the active network during teardown, which can still report the dying network
 * as connected.
 */
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
