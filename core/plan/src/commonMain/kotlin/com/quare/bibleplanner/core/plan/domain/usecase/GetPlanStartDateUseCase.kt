package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

class GetPlanStartDateUseCase(
    private val planRepository: PlanRepository,
) {
    operator fun invoke(): Flow<LocalDate?> = planRepository.getStartPlanTimestamp()
}
