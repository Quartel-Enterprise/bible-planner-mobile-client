package com.quare.bibleplanner.domain.usecase.impl

import com.quare.bibleplanner.core.plan.domain.usecase.GetPlanStartDateFlowUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.SetPlanStartTimeUseCase

internal class EnsureStartDateIsAvailableUseCase(
    private val getPlanStartDate: GetPlanStartDateFlowUseCase,
    private val setPlanStartTime: SetPlanStartTimeUseCase,
) {
    suspend operator fun invoke() {
        getPlanStartDate().collect { startDate ->
            if (startDate == null) {
                setPlanStartTime(SetPlanStartTimeUseCase.Strategy.Now)
            }
        }
    }
}
