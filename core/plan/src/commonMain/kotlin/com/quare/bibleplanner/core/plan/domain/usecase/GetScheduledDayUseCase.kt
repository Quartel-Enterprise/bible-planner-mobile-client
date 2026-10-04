package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.ScheduledDayModel
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import kotlinx.coroutines.flow.first

/*
 * Why: callers that only name a day read the cached plan instead of scoring the whole
 * Bible's read state for fields they never use.
 */
class GetScheduledDayUseCase(
    private val planRepository: PlanRepository,
    private val getPlannedReadDateForDayUseCase: GetPlannedReadDateForDayUseCase,
) : GetScheduledDay {
    override suspend fun invoke(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
    ): ScheduledDayModel? {
        val day = planRepository
            .getPlans(readingPlanType)
            .find { it.number == weekNumber }
            ?.days
            ?.find { it.number == dayNumber }
            ?: return null
        val startDate = planRepository.getStartPlanTimestamp().first()
        return ScheduledDayModel(
            number = day.number,
            passages = day.passages,
            plannedReadDate = startDate?.let {
                getPlannedReadDateForDayUseCase(
                    weekNumber = weekNumber,
                    dayNumber = dayNumber,
                    startDate = it,
                )
            },
        )
    }
}
