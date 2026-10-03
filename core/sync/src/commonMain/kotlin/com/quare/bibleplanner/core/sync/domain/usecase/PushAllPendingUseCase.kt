package com.quare.bibleplanner.core.sync.domain.usecase

import com.quare.bibleplanner.core.sync.domain.Synchronizer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

internal class PushAllPendingUseCase(
    private val synchronizers: List<Synchronizer>,
) : PushAllPending {
    override suspend fun invoke() {
        coroutineScope {
            synchronizers
                .map { synchronizer -> async { synchronizer.pushPendingOnce() } }
                .awaitAll()
        }
    }
}
