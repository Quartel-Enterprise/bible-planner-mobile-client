package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.ScheduledDayModel
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import kotlinx.coroutines.flow.first

/**
 * The day as the plan schedules it: what to read and when it was due, with no reading progress
 * attached. Callers that only need to name a day get it from the cached plan and one preference
 * read, instead of scoring the whole Bible's read state to fill in fields they never look at.
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
