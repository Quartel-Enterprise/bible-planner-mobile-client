package com.quare.bibleplanner.core.sync.data

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.sync.domain.Synchronizer
import com.quare.bibleplanner.core.sync.domain.usecase.ObserveSync
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.core.utils.suspendRunCatching
import io.github.jan.supabase.realtime.Realtime
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * Single session-scoped driver for every registered [Synchronizer]. The whole pipeline is scoped to
 * the authenticated session via [collectLatest]: sign-out or account switch cancels every child
 * (closing the realtime channels) and leaves local data untouched.
 *
 * Pull-on-connect is centralized here because [Realtime.status] is shared across datasets: on every
 * CONNECTED transition (cold start + reconnections) every synchronizer pulls its full snapshot.
 */
internal class SyncCoordinator(
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val synchronizers: List<Synchronizer>,
    private val realtime: Realtime,
    private val currentTimestampProvider: CurrentTimestampProvider,
) : ObserveSync {
    private val logger = Logger.withTag(LOG_TAG)

    override suspend fun invoke() {
        observeAuthenticatedUserId().collectLatest { userId ->
            if (userId == null) return@collectLatest
            coroutineScope {
                val now = currentTimestampProvider.getCurrentTimestamp()
                synchronizers.forEach { it.seed(now) }
                synchronizers.forEach { synchronizer ->
                    launch { synchronizer.runPushLoop() }
                    launch { synchronizer.observeRealtime() }
                }
                launch { pullOnConnected() }
            }
        }
    }

    private suspend fun pullOnConnected() {
        realtime.status
            .filter { it == Realtime.Status.CONNECTED }
            .collect {
                synchronizers.forEach { synchronizer ->
                    suspendRunCatching { synchronizer.pullSnapshot() }
                        .onFailure { error -> logger.e(error) { "Failed to pull snapshot" } }
                }
            }
    }

    private companion object {
        const val LOG_TAG = "Sync"
    }
}
