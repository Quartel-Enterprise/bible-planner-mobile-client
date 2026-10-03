package com.quare.bibleplanner.core.sync.domain

interface Synchronizer {
    suspend fun seed(now: Long)

    suspend fun runPushLoop()

    suspend fun pushPendingOnce()

    suspend fun observeRealtime()

    suspend fun fetchSnapshot(): FetchedSnapshot

    suspend fun clearLocal()
}
