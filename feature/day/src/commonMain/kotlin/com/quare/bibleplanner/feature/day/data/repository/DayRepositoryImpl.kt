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
    override fun getDayByWeekAndDayFlow(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
    ): Flow<DayModel?> {
        return dayLocalDataSource
            .getDayByWeekAndDayFlow(weekNumber, dayNumber, readingPlanType.name)
            .map { entity ->
                val dayModel = getDayModelFromPlans(weekNumber, dayNumber, readingPlanType) ?: return@map null
                dayEntityToModelMapper.map(entity, dayModel)
            }
    }

    override suspend fun getDayByWeekAndDay(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
    ): DayModel? {
        val dayModel = getDayModelFromPlans(weekNumber, dayNumber, readingPlanType) ?: return null
        val entity = dayLocalDataSource.getDayByWeekAndDay(weekNumber, dayNumber, readingPlanType.name)
        return dayEntityToModelMapper.map(entity, dayModel)
    }

    override suspend fun updateDayReadStatus(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
        isRead: Boolean,
        readTimestamp: Long?,
    ) {
        dayLocalDataSource.updateDayReadStatus(weekNumber, dayNumber, readingPlanType.name, isRead, readTimestamp)
    }

    override suspend fun updateDayNotes(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
        notes: String?,
    ) {
        dayLocalDataSource.updateDayNotes(weekNumber, dayNumber, readingPlanType.name, notes)
    }

    private suspend fun getDayModelFromPlans(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
    ): DayModel? {
        val plans = planRepository.getPlans(readingPlanType)
        val week = plans.find { it.number == weekNumber } ?: return null
        return week.days.find { it.number == dayNumber }
    }
}
