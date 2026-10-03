package com.quare.bibleplanner.core.sync.domain

interface Synchronizer {
    suspend fun seed(now: Long)

    // Why: suspends forever; run it in its own coroutine.
    suspend fun runPushLoop()

    suspend fun pushPendingOnce()

    // Why: suspends forever; run it in its own coroutine.
    suspend fun observeRealtime()

    suspend fun fetchSnapshot(): FetchedSnapshot

    // Why: logout wipe must not schedule a push of the cleared state.
    suspend fun clearLocal()
}
