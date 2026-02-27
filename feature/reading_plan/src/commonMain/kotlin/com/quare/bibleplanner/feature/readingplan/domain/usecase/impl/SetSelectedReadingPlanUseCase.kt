package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import com.quare.bibleplanner.feature.readingplan.domain.usecase.SetSelectedReadingPlan

internal class SetSelectedReadingPlanUseCase(
    private val repository: PlanRepository,
) : SetSelectedReadingPlan {
    override suspend operator fun invoke(readingPlanType: ReadingPlanType) {
        repository.setSelectedReadingPlan(readingPlanType)
    }
}
