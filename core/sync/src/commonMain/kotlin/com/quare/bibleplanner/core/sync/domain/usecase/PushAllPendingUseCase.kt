package com.quare.bibleplanner.core.sync.domain.usecase

import com.quare.bibleplanner.core.sync.domain.Synchronizer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

internal class PushAllPendingUseCase(
    private val synchronizers: List<Synchronizer>,
) : PushAllPending {
    /*
     * Why: pushed concurrently to keep the logout flush within its timeout; one failure
     * cancels the rest and propagates so logout aborts and pending data is retried.
     */
    override suspend fun invoke() {
        coroutineScope {
            synchronizers
                .map { synchronizer -> async { synchronizer.pushPendingOnce() } }
                .awaitAll()
        }
    }
}
