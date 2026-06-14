package com.quare.bibleplanner.core.clear.domain

import com.quare.bibleplanner.core.books.domain.usecase.ClearLocalReadingDataUseCase
import com.quare.bibleplanner.core.sync.domain.usecase.ClearAllSyncedLocalData
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Clears the user's local reading progress (database) and all synced data (favorites + reading-plan
 * preferences) in parallel. The synced datasets own their own resets via the sync engine, so new
 * synced data is wiped automatically.
 */
internal class ClearLocalUserDataUseCase(
    private val clearLocalReadingData: ClearLocalReadingDataUseCase,
    private val clearAllSyncedLocalData: ClearAllSyncedLocalData,
) : ClearLocalUserData {
    override suspend fun invoke() {
        coroutineScope {
            launch { clearLocalReadingData() }
            launch { clearAllSyncedLocalData() }
        }
    }
}
