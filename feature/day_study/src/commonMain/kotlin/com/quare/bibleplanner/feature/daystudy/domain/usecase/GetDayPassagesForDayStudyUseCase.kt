package com.quare.bibleplanner.feature.daystudy.domain.usecase

import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetDayPassagesForDayStudyUseCase(
    private val getPlansByWeekUseCase: GetPlansByWeekUseCase,
) {
    operator fun invoke(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
    ): Flow<List<PassageModel>?> = getPlansByWeekUseCase().map { plans ->
        val weeks = when (readingPlanType) {
            ReadingPlanType.CHRONOLOGICAL -> plans.chronologicalOrder
            ReadingPlanType.BOOKS -> plans.booksOrder
        }
        weeks
            .find { it.number == weekNumber }
            ?.days
            ?.find { it.number == dayNumber }
            ?.passages
    }
}
