package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository

/**
 * Resets the user's reading-plan local state on logout: the start date is anchored to today and the
 * selected plan type is dropped (falls back to the default). This way another account signing in on
 * the same device starts a fresh plan from today instead of inheriting the previous schedule.
 */
class ClearReadingPlanUseCase(
    private val planRepository: PlanRepository,
    private val currentTimestampProvider: CurrentTimestampProvider,
) {
    suspend operator fun invoke() {
        planRepository.resetPlan(startDate = currentTimestampProvider.getCurrentTimestamp())
    }
}
