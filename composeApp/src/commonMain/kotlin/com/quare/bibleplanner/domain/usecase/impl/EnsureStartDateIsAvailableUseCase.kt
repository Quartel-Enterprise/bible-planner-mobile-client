package com.quare.bibleplanner.domain.usecase.impl

import com.quare.bibleplanner.core.plan.domain.usecase.GetPlanStartDateFlowUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.SetPlanStartTimeUseCase
import kotlinx.coroutines.flow.first

internal class EnsureStartDateIsAvailableUseCase(
    private val getPlanStartDateFlow: GetPlanStartDateFlowUseCase,
    private val setPlanStartTime: SetPlanStartTimeUseCase,
) {
    suspend operator fun invoke() {
        getPlanStartDateFlow().first() ?: setPlanStartTime(SetPlanStartTimeUseCase.Strategy.Now)
    }
}
