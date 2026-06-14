package com.quare.bibleplanner.core.sync.domain

import kotlinx.coroutines.flow.Flow

/**
 * Local source of truth for one synced dataset. The UI observes the local state directly; the sync
 * engine reconciles it with the backend using Last-Write-Wins by timestamp.
 *
 * Implementations adapt a concrete local store (a Room DAO, etc.) to the generic engine:
 *  - [E] is the local entity that carries the per-row sync metadata (a pending flag + an updatedAt).
 *  - [D] is the remote payload pushed to / received from the backend.
 */
interface SyncLocalStore<E, D> {
    /** Rows with local changes not yet acknowledged by the backend. */
    fun pendingFlow(): Flow<List<E>>

    /** Snapshot of the currently pending rows (for a one-shot flush, e.g. before logout). */
    suspend fun getPending(): List<E>

    /**
     * Clears the pending flag for [entity], but only if the row was not re-touched meanwhile (guarded
     * by the entity's own updatedAt), so a change made while the push was in flight is not lost.
     */
    suspend fun markSynced(entity: E)

    /**
     * Applies a remote row with Last-Write-Wins semantics: it must overwrite the local row only when
     * the row is not pending and the remote change is strictly newer, so the echo of our own write and
     * stale remote rows are no-ops.
     */
    suspend fun applyRemote(dto: D)

    /** Builds the remote payload for a pending [entity]. */
    fun toDto(
        userId: String,
        entity: E,
    ): D

    /**
     * One-time per-session hook for legacy data migration or provisional defaults. [now] is the
     * current wall-clock timestamp. Default: no-op.
     */
    suspend fun seed(now: Long) = Unit

    /** Wipes this dataset's local state on logout, without scheduling a push. */
    suspend fun clearLocal()
}
