package com.quare.bibleplanner.core.provider.connectivity

import kotlinx.coroutines.flow.Flow

/*
 * Why: a faster reconnection signal than the Supabase realtime socket status, so pending
 * REST upserts flush as soon as connectivity returns instead of waiting for the websocket.
 */
fun interface NetworkConnectivityObserver {
    fun observe(): Flow<Boolean>
}
