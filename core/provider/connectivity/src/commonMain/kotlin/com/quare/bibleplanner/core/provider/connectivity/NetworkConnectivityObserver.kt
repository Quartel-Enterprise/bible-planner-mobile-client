package com.quare.bibleplanner.core.provider.connectivity

import kotlinx.coroutines.flow.Flow

/**
 * Observes the device's network connectivity. Emits `true` when the OS reports an available network
 * and `false` otherwise, emitting the current value on subscription and then on every change.
 *
 * This is a faster reconnection signal than the Supabase realtime socket status: it lets pending
 * writes (plain REST upserts) flush the moment connectivity returns, without waiting for the
 * websocket to detect the drop and reconnect.
 */
interface NetworkConnectivityObserver {
    fun observe(): Flow<Boolean>
}
