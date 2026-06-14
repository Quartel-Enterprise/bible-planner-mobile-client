package com.quare.bibleplanner.core.clear.domain

import com.quare.bibleplanner.core.books.domain.usecase.ClearLocalReadingDataUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.ClearReadingPlanUseCase
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Clears the user's reading data (database) and reading-plan preferences (DataStore) in parallel —
 * they are independent storage backends, so there is no shared lock to serialize them.
 */
internal class ClearLocalUserDataUseCase(
    private val clearLocalReadingData: ClearLocalReadingDataUseCase,
    private val clearReadingPlan: ClearReadingPlanUseCase,
) : ClearLocalUserData {
    override suspend fun invoke() {
        coroutineScope {
            launch { clearLocalReadingData() }
            launch { clearReadingPlan() }
        }
    }
}
