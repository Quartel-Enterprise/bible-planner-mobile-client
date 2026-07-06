package com.quare.bibleplanner.core.clear.domain

import com.quare.bibleplanner.core.books.domain.usecase.ClearLocalReadingDataUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.EnsureDefaultPlanStartDateUseCase
import com.quare.bibleplanner.core.sync.domain.usecase.ClearAllSyncedLocalData
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Clears the user's local reading progress (database) and all synced data (favorites + reading-plan
 * preferences) in parallel. The synced datasets own their own resets via the sync engine, so new
 * synced data is wiped automatically.
 *
 * Wiping the synced preferences also removes the reading-plan start date, so once the sync data is
 * cleared we re-seed today as the provisional default ([EnsureDefaultPlanStartDateUseCase]); the
 * start date must never end up empty after logout.
 */
internal class ClearLocalUserDataUseCase(
    private val clearLocalReadingData: ClearLocalReadingDataUseCase,
    private val clearAllSyncedLocalData: ClearAllSyncedLocalData,
    private val clearDayStudyLocalData: ClearDayStudyLocalData,
    private val ensureDefaultPlanStartDate: EnsureDefaultPlanStartDateUseCase,
) : ClearLocalUserData {
    override suspend fun invoke() {
        coroutineScope {
            launch { clearLocalReadingData() }
            launch { clearDayStudyLocalData() }
            launch {
                clearAllSyncedLocalData()
                ensureDefaultPlanStartDate()
            }
        }
    }
}
