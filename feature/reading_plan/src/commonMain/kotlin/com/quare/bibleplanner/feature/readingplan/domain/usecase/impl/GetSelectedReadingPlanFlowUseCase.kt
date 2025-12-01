package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.feature.readingplan.domain.repository.ReadingPlanRepository
import com.quare.bibleplanner.feature.readingplan.domain.usecase.GetSelectedReadingPlanFlow
import kotlinx.coroutines.flow.Flow

internal class GetSelectedReadingPlanFlowUseCase(
    private val repository: ReadingPlanRepository,
) : GetSelectedReadingPlanFlow {
    override operator fun invoke(): Flow<ReadingPlanType> = repository.getSelectedReadingPlanFlow()
}
