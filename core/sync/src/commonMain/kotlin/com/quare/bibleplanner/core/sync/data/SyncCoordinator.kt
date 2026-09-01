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

/**
 * Single session-scoped driver for every registered [Synchronizer]. The whole pipeline is scoped to
 * the authenticated session via [collectLatest]: sign-out or account switch cancels every child
 * (closing the realtime channels).
 *
 * When a different account signs in without a logout in between (logout is what normally clears local
 * data), the previous account's synced state is wiped before seeding/pulling. A pull only upserts the
 * remote snapshot — it never removes local rows the new account does not have — so without this wipe
 * the previous account's reads/favorites would leak into the new account and across devices.
 *
 * Pull-on-connect is centralized here because [Realtime.status] is shared across datasets: on every
 * CONNECTED transition (cold start + reconnections) every synchronizer pulls its full snapshot.
 *
 * Realtime channels only stay subscribed while the app is foregrounded ([AppForegroundStateHolder]):
 * with no subscribed channel the Supabase client disconnects the websocket, so a background session
 * refresh no longer reaches Realtime's internal setAuth, whose unsupervised coroutine crashes the
 * app when the socket died meanwhile ("Websocket not yet initialized" / TokenExpiredException).
 * Anything missed while backgrounded is covered by the snapshot pull on the reconnect that follows.
 */
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
