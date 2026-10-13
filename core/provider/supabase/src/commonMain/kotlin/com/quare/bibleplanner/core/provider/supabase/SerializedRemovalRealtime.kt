package com.quare.bibleplanner.core.provider.supabase

import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/*
 * Why: supabase-kt (still in 3.9.0-beta-1) schedules a deferred disconnect on every removeChannel
 * that leaves no subscriptions, but keeps only the last job, so addChannel can't cancel the others.
 * Concurrent removals, or removals after removeAllChannels, then disconnect live channels later in
 * the foreground and realtime silently stops. Remove once supabase-kt#1414 ships:
 * https://github.com/supabase-community/supabase-kt/issues/1414
 */
internal class SerializedRemovalRealtime(
    private val delegate: Realtime,
) : Realtime by delegate {
    private val removalMutex = Mutex()

    override suspend fun removeChannel(channel: RealtimeChannel) {
        removalMutex.withLock {
            // Why: a channel already gone (or replaced under its topic) would only schedule another disconnect.
            if (delegate.subscriptions[channel.topic] === channel) {
                delegate.removeChannel(channel)
            }
        }
    }

    override suspend fun removeAllChannels() {
        removalMutex.withLock { delegate.removeAllChannels() }
    }
}
