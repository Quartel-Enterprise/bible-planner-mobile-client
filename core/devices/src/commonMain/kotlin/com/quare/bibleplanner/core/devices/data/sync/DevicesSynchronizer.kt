package com.quare.bibleplanner.core.devices.data.sync

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.devices.data.UserDevicesRemoteStore
import com.quare.bibleplanner.core.devices.data.local.UserDeviceLocalStore
import com.quare.bibleplanner.core.devices.data.model.DeviceChange
import com.quare.bibleplanner.core.provider.connectivity.NetworkConnectivityObserver
import com.quare.bibleplanner.core.provider.room.entity.UserDeviceEntity
import com.quare.bibleplanner.core.sync.domain.Synchronizer
import com.quare.bibleplanner.core.user.domain.usecase.GetAuthenticatedUserId
import com.quare.bibleplanner.core.utils.suspendRunCatching
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Offline-first + realtime sync for the connected-devices list. Room is the source of truth the UI
 * observes; the device rows are server-authoritative except for the user-editable `name`, which is
 * reconciled Last-Write-Wins. Unlike [com.quare.bibleplanner.core.sync.data.OfflineFirstSynchronizer]
 * this handles remote DELETEs (a session revoked elsewhere removes the row).
 */
internal class DevicesSynchronizer(
    private val localStore: UserDeviceLocalStore,
    private val remoteStore: UserDevicesRemoteStore,
    private val networkConnectivityObserver: NetworkConnectivityObserver,
    private val getAuthenticatedUserId: GetAuthenticatedUserId,
) : Synchronizer {
    private val logger = Logger.withTag(LOG_TAG)
    private val initialBackoff: Duration = 2.seconds
    private val maxBackoff: Duration = 60.seconds

    override suspend fun seed(now: Long) = Unit

    override suspend fun runPushLoop() {
        getAuthenticatedUserId() ?: return
        combine(
            localStore.pendingFlow(),
            networkConnectivityObserver.observe(),
        ) { pending, isOnline -> pending to isOnline }
            .collectLatest { (pending, isOnline) ->
                if (pending.isEmpty() || !isOnline) return@collectLatest
                var backoff = initialBackoff
                while (true) {
                    suspendRunCatching { push(pending) }
                        .onSuccess { return@collectLatest }
                        .onFailure { error ->
                            logger.e(error) { "Failed to push renamed devices; retrying in $backoff" }
                            delay(backoff)
                            backoff = (backoff * BACKOFF_FACTOR).coerceAtMost(maxBackoff)
                        }
                }
            }
    }

    override suspend fun pushPendingOnce() {
        getAuthenticatedUserId() ?: return
        val pending = localStore.getPending()
        if (pending.isNotEmpty()) push(pending)
    }

    private suspend fun push(pending: List<UserDeviceEntity>) {
        pending.forEach { entity ->
            remoteStore.rename(
                deviceRowId = entity.id,
                name = entity.name,
                updatedAt = Instant.fromEpochMilliseconds(entity.updatedAt).toString(),
            )
            localStore.markNameSynced(entity)
        }
    }

    override suspend fun observeRealtime() {
        val userId = getAuthenticatedUserId() ?: return
        remoteStore
            .observeChanges(userId)
            .catch { error -> logger.e(error) { "Realtime devices stream failed" } }
            .collect { change ->
                when (change) {
                    is DeviceChange.Upserted -> localStore.applyRemote(change.dto)
                    is DeviceChange.Removed -> localStore.deleteById(change.id)
                }
            }
    }

    override suspend fun pullSnapshot() {
        val userId = getAuthenticatedUserId() ?: return
        val dtos = remoteStore.fetch(userId)
        dtos.forEach { localStore.applyRemote(it) }
        localStore.retainOnly(dtos.map { it.id })
    }

    override suspend fun clearLocal() {
        localStore.clearLocal()
    }

    private companion object {
        const val LOG_TAG = "DevicesSync"
        const val BACKOFF_FACTOR = 2
    }
}
