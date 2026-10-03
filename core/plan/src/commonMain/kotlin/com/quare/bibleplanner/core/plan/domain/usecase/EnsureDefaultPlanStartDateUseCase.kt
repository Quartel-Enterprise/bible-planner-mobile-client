package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository

class EnsureDefaultPlanStartDateUseCase(
    private val planRepository: PlanRepository,
    private val currentTimestampProvider: CurrentTimestampProvider,
) {
    suspend operator fun invoke() {
        planRepository.seedDefaultStartDate(currentTimestampProvider.getCurrentTimestamp())
    }
}
