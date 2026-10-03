package com.quare.bibleplanner.core.sync.domain

import kotlinx.coroutines.flow.Flow

interface SyncLocalStore<E, D> {
    fun observePending(): Flow<List<E>>

    suspend fun getPending(): List<E>

    // Why: clear the pending flag only if the row was not re-touched (updatedAt guard), so a
    // change made while the push was in flight is not lost.
    suspend fun markSynced(entity: E)

    // Why: overwrite only non-pending rows with a strictly newer remote change, so the echo
    // of our own write and stale remote rows are no-ops.
    suspend fun applyRemote(dto: D)

    fun toDto(
        userId: String,
        entity: E,
    ): D

    suspend fun seed(now: Long) = Unit

    suspend fun adoptProvisionalDefaults(now: Long) = Unit

    // Why: the logout wipe must not schedule a push, or it would propagate to the account's
    // remote data.
    suspend fun clearLocal()
}
