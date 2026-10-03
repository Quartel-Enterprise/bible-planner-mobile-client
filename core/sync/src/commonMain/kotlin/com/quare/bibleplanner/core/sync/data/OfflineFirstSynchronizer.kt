package com.quare.bibleplanner.core.sync.data

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.provider.connectivity.NetworkConnectivityObserver
import com.quare.bibleplanner.core.sync.domain.FetchedSnapshot
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

class OfflineFirstSynchronizer<E, D>(
    private val localStore: SyncLocalStore<E, D>,
    private val remoteStore: SyncRemoteStore<D>,
    private val networkConnectivityObserver: NetworkConnectivityObserver,
    private val getAuthenticatedUserId: GetAuthenticatedUserId,
    private val currentTimestampProvider: CurrentTimestampProvider,
    logTag: String,
) : Synchronizer {
    private val logger = Logger.withTag(logTag)
    private val initialBackoff: Duration = 2.seconds
    private val maxBackoff: Duration = 60.seconds

    override suspend fun seed(now: Long) {
        localStore.seed(now)
    }

    override suspend fun runPushLoop() {
        combine(
            localStore.observePending(),
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

    override suspend fun fetchSnapshot(): FetchedSnapshot {
        val userId = getAuthenticatedUserId() ?: return FetchedSnapshot {}
        val dtos = remoteStore.fetch(userId)
        return FetchedSnapshot {
            dtos.forEach { localStore.applyRemote(it) }
            localStore.adoptProvisionalDefaults(currentTimestampProvider.getCurrentTimestamp())
        }
    }

    override suspend fun clearLocal() {
        localStore.clearLocal()
    }

    private companion object {
        const val BACKOFF_FACTOR = 2
    }
}
