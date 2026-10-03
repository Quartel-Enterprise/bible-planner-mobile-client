package com.quare.bibleplanner.core.sync.data

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.AppForegroundStateHolder
import com.quare.bibleplanner.core.sync.domain.Synchronizer
import com.quare.bibleplanner.core.sync.domain.usecase.ObserveSync
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import io.github.jan.supabase.realtime.Realtime
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

internal class SyncCoordinator(
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val synchronizers: List<Synchronizer>,
    private val snapshotPuller: SnapshotPuller,
    private val realtime: Realtime,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val appForegroundStateHolder: AppForegroundStateHolder,
) : ObserveSync {
    override suspend fun invoke() {
        var previousUserId: String? = null
        observeAuthenticatedUserId().collectLatest { userId ->
            if (userId == null) {
                previousUserId = null
                return@collectLatest
            }
            if (previousUserId != null && previousUserId != userId) {
                synchronizers.forEach { it.clearLocal() }
            }
            previousUserId = userId
            coroutineScope {
                val now = currentTimestampProvider.getCurrentTimestamp()
                synchronizers.forEach { it.seed(now) }
                synchronizers.forEach { synchronizer ->
                    launch { synchronizer.runPushLoop() }
                    launch { observeRealtimeWhileForegrounded(synchronizer) }
                }
                launch { pullOnConnected() }
            }
        }
    }

    private suspend fun observeRealtimeWhileForegrounded(synchronizer: Synchronizer) {
        appForegroundStateHolder.isForeground.collectLatest { isForeground ->
            if (isForeground) {
                synchronizer.observeRealtime()
            }
        }
    }

    private suspend fun pullOnConnected() {
        realtime.status
            .filter { it == Realtime.Status.CONNECTED }
            .collect { snapshotPuller.pullAll() }
    }
}
