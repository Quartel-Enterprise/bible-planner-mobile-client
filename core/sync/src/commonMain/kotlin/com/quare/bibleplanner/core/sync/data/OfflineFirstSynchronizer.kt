package com.quare.bibleplanner.core.sync.data

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.provider.connectivity.NetworkConnectivityObserver
import com.quare.bibleplanner.core.sync.domain.SyncLocalStore
import com.quare.bibleplanner.core.sync.domain.SyncRemoteStore
import com.quare.bibleplanner.core.sync.domain.Synchronizer
import com.quare.bibleplanner.core.user.domain.usecase.GetAuthenticatedUserId
import com.quare.bibleplanner.core.utils.suspendRunCatching
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Generic offline-first + realtime synchronizer for one dataset. The local Room state is the source
 * of truth the UI observes; this reconciles it with Supabase using Last-Write-Wins by timestamp:
 *
 *  - **push** ([runPushLoop]) — pending local changes are upserted whenever there is something
 *    pending and the device is online (gated on OS connectivity so they flush the moment the network
 *    returns); the pending flag is cleared only if the row was not re-touched meanwhile (guarded in
 *    [SyncLocalStore.markSynced]).
 *  - **pull** ([pullSnapshot]) — the full remote set is fetched and applied; driven by
 *    [SyncCoordinator] on every realtime CONNECTED transition (including cold start). Covers changes
 *    missed while offline.
 *  - **realtime** ([observeRealtime]) — live inserts/updates are applied as they arrive.
 *
 * Remote writes go through [SyncLocalStore.applyRemote], which only overwrites strictly older,
 * non-pending rows.
 */
class OfflineFirstSynchronizer<E, D>(
    private val localStore: SyncLocalStore<E, D>,
    private val remoteStore: SyncRemoteStore<D>,
    private val networkConnectivityObserver: NetworkConnectivityObserver,
    private val getAuthenticatedUserId: GetAuthenticatedUserId,
    logTag: String,
) : Synchronizer {
    private val logger = Logger.withTag(logTag)
    private val initialBackoff: Duration = 2.seconds
    private val maxBackoff: Duration = 60.seconds

    override suspend fun seed(now: Long) {
        localStore.seed(now)
    }

    /**
     * Gating on OS connectivity (rather than the realtime socket status) flushes pending writes the
     * moment the network returns — the upsert is a plain REST call and doesn't need the websocket. The
     * bounded backoff only handles transient failures that happen while online; each new pending change
     * or reconnection re-triggers this block (via [collectLatest]) with a fresh backoff.
     *
     * The session is re-read at the start of every attempt (not captured once): if it is gone the loop
     * stops instead of pushing, since the Supabase client would otherwise fall back to the anonymous key
     * and the write would fail the row-level security policy — a permanent error the backoff would retry
     * forever.
     */
    override suspend fun runPushLoop() {
        combine(
            localStore.pendingFlow(),
            networkConnectivityObserver.observe(),
        ) { pending, isOnline -> pending to isOnline }
            .collectLatest { (pending, isOnline) ->
                if (pending.isEmpty() || !isOnline) return@collectLatest
                var backoff = initialBackoff
                while (true) {
                    val userId = getAuthenticatedUserId() ?: return@collectLatest
                    suspendRunCatching { push(userId, pending) }
                        .onSuccess { return@collectLatest }
                        .onFailure { error ->
                            logger.e(error) { "Failed to push pending changes; retrying in $backoff" }
                            delay(backoff)
                            backoff = (backoff * BACKOFF_FACTOR).coerceAtMost(maxBackoff)
                        }
                }
            }
    }

    override suspend fun pushPendingOnce() {
        val userId = getAuthenticatedUserId() ?: return
        val pending = localStore.getPending()
        if (pending.isEmpty()) return
        push(userId, pending)
    }

    private suspend fun push(
        userId: String,
        pending: List<E>,
    ) {
        remoteStore.upsert(pending.map { localStore.toDto(userId, it) })
        pending.forEach { localStore.markSynced(it) }
    }

    override suspend fun observeRealtime() {
        val userId = getAuthenticatedUserId() ?: return
        remoteStore
            .observeRemote(userId)
            .catch { error -> logger.e(error) { "Realtime stream failed" } }
            .collect(localStore::applyRemote)
    }

    override suspend fun pullSnapshot() {
        val userId = getAuthenticatedUserId() ?: return
        remoteStore.fetch(userId).forEach { localStore.applyRemote(it) }
    }

    override suspend fun clearLocal() {
        localStore.clearLocal()
    }

    private companion object {
        const val BACKOFF_FACTOR = 2
    }
}
