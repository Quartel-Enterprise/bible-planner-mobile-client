package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import kotlinx.coroutines.flow.first

class GetDayUseCase(
    private val getPlansByWeekUseCase: GetPlansByWeekUseCase,
) : GetDay {
    override suspend fun invoke(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
    ): DayModel? {
        val plans = getPlansByWeekUseCase().first()
        val weeks = when (readingPlanType) {
            ReadingPlanType.CHRONOLOGICAL -> plans.chronologicalOrder
            ReadingPlanType.BOOKS -> plans.booksOrder
        }
        return weeks.find { it.number == weekNumber }?.days?.find { it.number == dayNumber }
    }
}
