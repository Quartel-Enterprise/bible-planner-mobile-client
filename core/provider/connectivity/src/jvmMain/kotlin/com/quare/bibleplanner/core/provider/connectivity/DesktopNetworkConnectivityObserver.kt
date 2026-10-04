package com.quare.bibleplanner.core.provider.connectivity

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.NetworkInterface
import java.util.Collections
import kotlin.time.Duration

/*
 * Why: desktop has no OS push for connectivity changes, so this polls for an up, non-loopback
 * interface; realtime reconnects on its own, this only gives the push loop a back-online signal.
 */
internal class DesktopNetworkConnectivityObserver(
    private val pollInterval: Duration,
) : NetworkConnectivityObserver {
    override fun observe(): Flow<Boolean> = flow {
        var previous: Boolean? = null
        while (true) {
            val online = isOnline()
            if (online != previous) {
                emit(online)
                previous = online
            }
            delay(pollInterval)
        }
    }

    private fun isOnline(): Boolean = runCatching {
        Collections
            .list(NetworkInterface.getNetworkInterfaces())
            .any { it.isUp && !it.isLoopback }
    }.getOrDefault(true)
}
