package com.quare.bibleplanner.core.provider.connectivity

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

internal class WebNetworkConnectivityObserver(
    private val pollInterval: Duration,
) : NetworkConnectivityObserver {
    override fun observe(): Flow<Boolean> = flow {
        while (true) {
            emit(isNavigatorOnline())
            delay(pollInterval)
        }
    }.distinctUntilChanged()
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun isNavigatorOnline(): Boolean = js("navigator.onLine")
