package com.quare.bibleplanner.core.sync.domain

import kotlinx.coroutines.flow.Flow

interface SyncRemoteStore<D> {
    suspend fun upsert(dtos: List<D>)

    suspend fun fetch(userId: String): List<D>

    fun observeRemote(userId: String): Flow<D>
}
