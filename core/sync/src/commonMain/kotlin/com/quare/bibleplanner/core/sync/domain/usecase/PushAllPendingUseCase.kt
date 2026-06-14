package com.quare.bibleplanner.core.sync.domain.usecase

import com.quare.bibleplanner.core.sync.domain.Synchronizer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

internal class PushAllPendingUseCase(
    private val synchronizers: List<Synchronizer>,
) : PushAllPending {
    /**
     * Pushes every dataset concurrently: each [Synchronizer.pushPendingOnce] is a network round-trip,
     * so running them in parallel keeps the logout flush within its bounded timeout. A failure in any
     * one cancels the rest and propagates, so the flush (and logout) aborts and the still-pending data
     * is retried later.
     */
    override suspend fun invoke() {
        coroutineScope {
            synchronizers
                .map { synchronizer -> async { synchronizer.pushPendingOnce() } }
                .awaitAll()
        }
    }
}
