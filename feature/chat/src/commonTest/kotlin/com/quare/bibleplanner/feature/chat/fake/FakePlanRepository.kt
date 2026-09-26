package com.quare.bibleplanner.feature.chat.fake

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

internal class FakePlanRepository(
    private val weeksByPlan: Map<ReadingPlanType, List<WeekPlanModel>>,
    private val selectedPlan: ReadingPlanType,
) : PlanRepository {
    override suspend fun getPlans(readingPlanType: ReadingPlanType): List<WeekPlanModel> =
        weeksByPlan.getValue(readingPlanType)

    override suspend fun setStartPlanTimestamp(timestamp: Long) = error("unused")

    override fun getStartPlanTimestamp(): Flow<LocalDate?> = flowOf(null)

    override fun getSelectedReadingPlanFlow(): Flow<ReadingPlanType> = flowOf(selectedPlan)

    override suspend fun setSelectedReadingPlan(readingPlanType: ReadingPlanType) = error("unused")

    override suspend fun seedDefaultStartDate(timestamp: Long) = error("unused")
}
