package com.quare.bibleplanner.core.books.data.sync

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.books.data.datasource.FavoritesRemoteDataSource
import com.quare.bibleplanner.core.books.data.dto.BookFavoriteDto
import com.quare.bibleplanner.core.books.data.mapper.BookFavoriteMapper
import com.quare.bibleplanner.core.books.domain.usecase.ObserveBookFavoritesSync
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.provider.connectivity.NetworkConnectivityObserver
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.core.utils.suspendRunCatching
import io.github.jan.supabase.realtime.Realtime
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Drives offline-first favorite synchronization. The local Room state is the source of truth the UI
 * observes; this manager reconciles it with Supabase using Last-Write-Wins by timestamp:
 *
 *  - **push** — pending local changes are upserted whenever there is something pending and the device
 *    is online (gated on OS connectivity so they flush the moment the network returns); the pending
 *    flag is cleared only if the row was not re-toggled meanwhile (guarded in
 *    [BookDao.markFavoriteSynced]).
 *  - **pull** — on every realtime CONNECTED transition (including cold start), the full remote set is
 *    fetched and applied. This covers changes missed while offline.
 *  - **realtime** — live inserts/updates are applied as they arrive.
 *
 * Remote writes are applied via [BookDao.applyRemoteFavorite], which only overwrites strictly older,
 * non-pending rows — so the echo of our own write and stale remote rows are no-ops.
 *
 * The whole pipeline is scoped to the authenticated session via [collectLatest]: sign-out or account
 * switch cancels every child (closing the realtime channel) and leaves local data untouched.
 */
internal class BookFavoritesSyncManager(
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val favoritesRemoteDataSource: FavoritesRemoteDataSource,
    private val bookDao: BookDao,
    private val bookFavoriteMapper: BookFavoriteMapper,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val networkConnectivityObserver: NetworkConnectivityObserver,
) : ObserveBookFavoritesSync {
    private val logger = Logger.withTag(LOG_TAG)
    private val initialBackoff: Duration = 2.seconds
    private val maxBackoff: Duration = 60.seconds

    override suspend fun invoke() {
        observeAuthenticatedUserId().collectLatest { userId ->
            if (userId == null) return@collectLatest
            coroutineScope {
                bookDao.markLegacyFavoritesPending(currentTimestampProvider.getCurrentTimestamp())
                launch { pushPendingLoop(userId) }
                launch { applyRemoteChanges(userId) }
                launch { pullOnConnected(userId) }
            }
        }
    }

    /**
     * Pushes pending local changes whenever there is something to push **and** the device is online.
     * Gating on OS connectivity (rather than the realtime socket status) flushes pending writes the
     * moment the network returns — the upsert is a plain REST call and doesn't need the websocket —
     * instead of waiting out a grown backoff or the socket's own reconnect detection. We also don't
     * burn retries while offline. The bounded backoff only handles transient failures that happen
     * while online; each new pending change or reconnection re-triggers this block (via
     * [collectLatest]) with a fresh backoff.
     */
    private suspend fun pushPendingLoop(userId: String) {
        combine(
            bookDao.getPendingFavoriteSyncBooksFlow(),
            networkConnectivityObserver.observe(),
        ) { pending, isOnline -> pending to isOnline }
            .collectLatest { (pending, isOnline) ->
                if (pending.isEmpty() || !isOnline) return@collectLatest
                var backoff = initialBackoff
                while (true) {
                    suspendRunCatching {
                        val dtos = pending.map { bookFavoriteMapper.toDto(userId, it) }
                        favoritesRemoteDataSource.upsertFavorites(dtos)
                        pending.forEach { entity ->
                            entity.favoriteUpdatedAt?.let { syncedUpdatedAt ->
                                bookDao.markFavoriteSynced(entity.id, syncedUpdatedAt)
                            }
                        }
                    }.onSuccess {
                        return@collectLatest
                    }.onFailure { error ->
                        logger.e(error) { "Failed to push pending favorites; retrying in $backoff" }
                        delay(backoff)
                        backoff = (backoff * BACKOFF_FACTOR).coerceAtMost(maxBackoff)
                    }
                }
            }
    }

    private suspend fun applyRemoteChanges(userId: String) {
        favoritesRemoteDataSource
            .observeRemoteFavorites(userId)
            .catch { error -> logger.e(error) { "Realtime favorites stream failed" } }
            .collect(::applyRemote)
    }

    private suspend fun pullOnConnected(userId: String) {
        favoritesRemoteDataSource
            .getRealtimeStatusFlow()
            .filter { it == Realtime.Status.CONNECTED }
            .collect {
                suspendRunCatching {
                    favoritesRemoteDataSource.fetchFavorites(userId).forEach { applyRemote(it) }
                }.onFailure { error -> logger.e(error) { "Failed to pull favorites snapshot" } }
            }
    }

    private suspend fun applyRemote(dto: BookFavoriteDto) {
        bookDao.applyRemoteFavorite(
            bookId = dto.bookId,
            isFavorite = dto.isFavorite,
            remoteUpdatedAt = bookFavoriteMapper.toEpochMillis(dto.updatedAt),
        )
    }

    private companion object {
        const val LOG_TAG = "BookFavoritesSync"
        const val BACKOFF_FACTOR = 2
    }
}
