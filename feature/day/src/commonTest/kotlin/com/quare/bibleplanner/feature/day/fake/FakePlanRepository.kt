package com.quare.bibleplanner.feature.day.fake

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

internal class FakePlanRepository(
    private val plans: Map<ReadingPlanType, List<WeekPlanModel>>,
    private val startDate: LocalDate?,
) : PlanRepository {
    override suspend fun getPlans(readingPlanType: ReadingPlanType): List<WeekPlanModel> =
        plans[readingPlanType].orEmpty()

    override suspend fun setStartPlanTimestamp(timestamp: Long) = error("unused")

    override fun getStartPlanTimestamp(): Flow<LocalDate?> = flowOf(startDate)

    override fun getSelectedReadingPlanFlow(): Flow<ReadingPlanType> = error("unused")

    override suspend fun setSelectedReadingPlan(readingPlanType: ReadingPlanType) = error("unused")

    override suspend fun seedDefaultStartDate(timestamp: Long) = error("unused")
}
