package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository

class DeletePlanStartDateUseCase(
    private val planRepository: PlanRepository,
) {
    suspend operator fun invoke() {
        planRepository.deleteStartPlanTimestamp()
    }
}
