package com.quare.bibleplanner.core.sync.domain

/**
 * One dataset's sync unit, driven by [com.quare.bibleplanner.core.sync.data.SyncCoordinator]. The
 * non-generic supertype lets the coordinator hold heterogeneous datasets together;
 * [com.quare.bibleplanner.core.sync.data.OfflineFirstSynchronizer] implements it generically.
 */
interface Synchronizer {
    /** One-time per-session hook (legacy migration, provisional defaults). */
    suspend fun seed(now: Long)

    /** Continuously pushes pending local changes while online; suspends forever. */
    suspend fun runPushLoop()

    /** Pushes the current pending set once (best-effort flush, e.g. before logout). */
    suspend fun pushPendingOnce()

    /** Applies live remote changes; suspends forever. */
    suspend fun observeRealtime()

    /** Fetches and applies the full remote snapshot (used on every realtime reconnection). */
    suspend fun pullSnapshot()

    /** Wipes this dataset's local state on logout, without scheduling a push. */
    suspend fun clearLocal()
}
