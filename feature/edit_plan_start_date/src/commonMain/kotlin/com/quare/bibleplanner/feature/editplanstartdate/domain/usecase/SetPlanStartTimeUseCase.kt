package com.quare.bibleplanner.feature.editplanstartdate.domain.usecase

import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository

internal class SetPlanStartTimeUseCase(
    private val planRepository: PlanRepository,
) {
    suspend operator fun invoke(timestamp: Long) {
        planRepository.setStartPlanTimestamp(timestamp)
    }
}

