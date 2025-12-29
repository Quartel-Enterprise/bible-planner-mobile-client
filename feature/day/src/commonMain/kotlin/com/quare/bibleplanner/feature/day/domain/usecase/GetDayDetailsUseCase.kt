package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.domain.repository.DayRepository
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetDayDetailsUseCase(
    private val getPlansByWeekUseCase: GetPlansByWeekUseCase,
    private val dayRepository: DayRepository,
) {
    operator fun invoke(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
    ): Flow<DayModel?> {
        return combine(
            getPlansByWeekUseCase(),
            dayRepository.getDayByWeekAndDayFlow(weekNumber, dayNumber),
        ) { plansModel, dayFromRepository ->
            val weeks = when (readingPlanType) {
                ReadingPlanType.CHRONOLOGICAL -> plansModel.chronologicalOrder
                ReadingPlanType.BOOKS -> plansModel.booksOrder
            }
            val week = weeks.find { it.number == weekNumber } ?: return@combine null
            val dayFromPlans = week.days.find { it.number == dayNumber } ?: return@combine null

            // Use the day from repository (which has readTimestamp) but merge with updated read status from plans
            dayFromRepository?.copy(
                passages = dayFromPlans.passages,
                isRead = dayFromPlans.isRead,
                totalVerses = dayFromPlans.totalVerses,
                readVerses = dayFromPlans.readVerses,
                readTimestamp = dayFromRepository.readTimestamp, // Preserve readTimestamp from repository
                plannedReadDate = dayFromPlans.plannedReadDate, // Preserve plannedReadDate from plans
            ) ?: dayFromPlans.copy(
                readTimestamp = null, // Ensure readTimestamp is set even if dayFromRepository is null
            )
        }
    }
}
