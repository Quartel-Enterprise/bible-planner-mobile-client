package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository

// Why: seeded as non-pending so it never overwrites a real remote start date the sync may pull;
// it becomes the account's date, and syncs, only once a full pull confirms none exists remotely.
class EnsureDefaultPlanStartDateUseCase(
    private val planRepository: PlanRepository,
    private val currentTimestampProvider: CurrentTimestampProvider,
) {
    suspend operator fun invoke() {
        planRepository.seedDefaultStartDate(currentTimestampProvider.getCurrentTimestamp())
    }
}
