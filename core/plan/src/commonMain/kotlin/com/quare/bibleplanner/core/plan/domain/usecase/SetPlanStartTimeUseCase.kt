package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository

class SetPlanStartTimeUseCase(
    private val planRepository: PlanRepository,
    private val currentTimestampProvider: CurrentTimestampProvider,
) {
    sealed interface Strategy {
        data class SpecificTime(
            val timestamp: Long,
        ) : Strategy

        data object Now : Strategy
    }

    suspend operator fun invoke(strategy: Strategy) {
        planRepository.setStartPlanTimestamp(
            when (strategy) {
                is Strategy.SpecificTime -> strategy.timestamp
                Strategy.Now -> currentTimestampProvider.getCurrentTimestamp()
            },
        )
    }
}
