package com.quare.bibleplanner.core.sync.domain.usecase

import com.quare.bibleplanner.core.sync.domain.Synchronizer

internal class ClearAllSyncedLocalDataUseCase(
    private val synchronizers: List<Synchronizer>,
) : ClearAllSyncedLocalData {
    override suspend fun invoke() {
        synchronizers.forEach { it.clearLocal() }
    }
}
