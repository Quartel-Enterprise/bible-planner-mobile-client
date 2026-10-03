package com.quare.bibleplanner.core.sync.domain

import kotlinx.coroutines.flow.Flow

interface SyncLocalStore<E, D> {
    fun observePending(): Flow<List<E>>

    suspend fun getPending(): List<E>

    suspend fun markSynced(entity: E)

    suspend fun applyRemote(dto: D)

    fun toDto(
        userId: String,
        entity: E,
    ): D

    suspend fun seed(now: Long) = Unit

    suspend fun adoptProvisionalDefaults(now: Long) = Unit

    suspend fun clearLocal()
}
