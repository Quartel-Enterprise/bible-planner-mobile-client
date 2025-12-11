package com.quare.bibleplanner.feature.day.data.repository

import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.domain.repository.DayRepository
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import com.quare.bibleplanner.feature.day.data.datasource.DayLocalDataSource
import com.quare.bibleplanner.feature.day.data.mapper.DayEntityToModelMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DayRepositoryImpl(
    private val dayLocalDataSource: DayLocalDataSource,
    private val planRepository: PlanRepository,
    private val dayEntityToModelMapper: DayEntityToModelMapper,
) : DayRepository {
    private val readingPlanType: ReadingPlanType = ReadingPlanType.CHRONOLOGICAL

    override fun getDayByWeekAndDayFlow(
        weekNumber: Int,
        dayNumber: Int,
    ): Flow<DayModel?> {
        return dayLocalDataSource
            .getDayByWeekAndDayFlow(weekNumber, dayNumber)
            .map { entity ->
                val dayModel = getDayModelFromPlans(weekNumber, dayNumber) ?: return@map null
                dayEntityToModelMapper.map(entity, dayModel)
            }
    }

    override suspend fun getDayByWeekAndDay(
        weekNumber: Int,
        dayNumber: Int,
    ): DayModel? {
        val dayModel = getDayModelFromPlans(weekNumber, dayNumber) ?: return null
        val entity = dayLocalDataSource.getDayByWeekAndDay(weekNumber, dayNumber)
        return dayEntityToModelMapper.map(entity, dayModel)
    }

    override suspend fun updateDayReadStatus(
        weekNumber: Int,
        dayNumber: Int,
        isRead: Boolean,
        readTimestamp: Long?,
    ) {
        dayLocalDataSource.updateDayReadStatus(weekNumber, dayNumber, isRead, readTimestamp)
    }

    private suspend fun getDayModelFromPlans(
        weekNumber: Int,
        dayNumber: Int,
    ): DayModel? {
        val plans = planRepository.getPlans(readingPlanType)
        val week = plans.find { it.number == weekNumber } ?: return null
        return week.days.find { it.number == dayNumber }
    }
}
