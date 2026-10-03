package com.quare.bibleplanner.core.clear.domain

import com.quare.bibleplanner.core.books.domain.usecase.ClearLocalReadingDataUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.EnsureDefaultPlanStartDateUseCase
import com.quare.bibleplanner.core.sync.domain.usecase.ClearAllSyncedLocalData
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal class ClearLocalUserDataUseCase(
    private val clearLocalReadingData: ClearLocalReadingDataUseCase,
    private val clearAllSyncedLocalData: ClearAllSyncedLocalData,
    private val clearDayStudyLocalData: ClearDayStudyLocalData,
    private val clearChapterStudyLocalData: ClearChapterStudyLocalData,
    private val clearChatLocalData: ClearChatLocalData,
    private val ensureDefaultPlanStartDate: EnsureDefaultPlanStartDateUseCase,
) : ClearLocalUserData {
    override suspend fun invoke() {
        coroutineScope {
            launch { clearLocalReadingData() }
            launch { clearDayStudyLocalData() }
            launch { clearChapterStudyLocalData() }
            launch { clearChatLocalData() }
            launch {
                clearAllSyncedLocalData()
                ensureDefaultPlanStartDate()
            }
        }
    }
}
