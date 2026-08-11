package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository

/**
 * Ensures a start date always exists by seeding today as a provisional, non-pending default when none
 * is set. Non-pending so it never overwrites a real remote value the sync engine may pull for this
 * account; it only becomes the account's start date — and starts syncing — once a full remote pull
 * confirms there is no remote one.
 */
class EnsureDefaultPlanStartDateUseCase(
    private val planRepository: PlanRepository,
    private val currentTimestampProvider: CurrentTimestampProvider,
) {
    suspend operator fun invoke() {
        planRepository.seedDefaultStartDate(currentTimestampProvider.getCurrentTimestamp())
    }
}
