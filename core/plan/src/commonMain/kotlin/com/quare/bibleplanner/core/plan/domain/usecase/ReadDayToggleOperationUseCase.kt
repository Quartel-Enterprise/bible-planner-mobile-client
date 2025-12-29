package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.model.plan.PlansModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import kotlinx.coroutines.flow.first

class ReadDayToggleOperationUseCase(
    private val setPlanStartTime: SetPlanStartTimeUseCase,
    private val deletePlanStartDate: DeletePlanStartDateUseCase,
    private val updateDayReadStatus: UpdateDayReadStatusUseCase,
    private val getPlansByWeek: GetPlansByWeekUseCase,
) {
    suspend operator fun invoke(
        newReadStatus: Boolean,
        weekNumber: Int,
        dayNumber: Int,
        selectedReadingPlan: ReadingPlanType,
    ) {
        val plans = getPlansByWeek().first()
        val allWeeks = plans.toAllWeeks()
        val containsReadDay = allWeeks.containsAtLeastOneReadDay()
        if (!containsReadDay) {
            setPlanStartTime(
                strategy = SetPlanStartTimeUseCase.Strategy.Now
            )
        } else if (plans.anyPlanWithJustOneReadDay() && !newReadStatus) {
            deletePlanStartDate()
        }
        updateDayReadStatus(
            weekNumber = weekNumber,
            dayNumber = dayNumber,
            isRead = newReadStatus,
            readingPlanType = selectedReadingPlan,
        )
    }

    private fun PlansModel.toAllWeeks(): List<WeekPlanModel> = chronologicalOrder + booksOrder
    private fun PlansModel.anyPlanWithJustOneReadDay(): Boolean =
        chronologicalOrder.isJustOneReadDay() || booksOrder.isJustOneReadDay()

    private fun List<WeekPlanModel>.containsAtLeastOneReadDay(): Boolean =
        count { week -> week.days.any { day -> day.isRead } } > 0

    private fun List<WeekPlanModel>.isJustOneReadDay(): Boolean =
        count { week -> week.days.count { day -> day.isRead } == 1 } == 1
}
