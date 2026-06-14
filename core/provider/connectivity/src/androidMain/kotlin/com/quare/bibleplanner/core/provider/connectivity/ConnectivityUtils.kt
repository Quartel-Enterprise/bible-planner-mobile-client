package com.quare.bibleplanner.core.provider.connectivity

import android.Manifest
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission

/**
 * Checks whether the device currently has a usable internet connection by inspecting the active
 * network's capabilities.
 *
 * @return `true` when the active network has verified internet access; `false` when there is no
 * active network or it lacks internet.
 */
@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
internal fun ConnectivityManager.isCurrentlyConnected(): Boolean {
    val capabilities = getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasInternet()
}

/**
 * Checks whether these capabilities advertise an actually usable internet connection.
 *
 * @return `true` when the network exposes the INTERNET capability and has been VALIDATED, so a
 * connected-but-no-internet link such as a captive portal does not count.
 */
internal fun NetworkCapabilities.hasInternet(): Boolean = hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
    hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
