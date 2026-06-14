package com.quare.bibleplanner.core.sync.domain

import kotlinx.coroutines.flow.Flow

/**
 * Backend side of one synced dataset (a Supabase table). [D] is the remote payload.
 */
interface SyncRemoteStore<D> {
    suspend fun upsert(dtos: List<D>)

    suspend fun fetch(userId: String): List<D>

    /**
     * Emits remote rows (inserts and updates) for [userId] in real time. Deletes are ignored: state
     * changes are modeled as upserts, so a delete carries no state to apply.
     */
    fun observeRemote(userId: String): Flow<D>
}
