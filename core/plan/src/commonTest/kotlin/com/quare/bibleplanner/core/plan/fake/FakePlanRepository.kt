package com.quare.bibleplanner.core.plan.fake

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate

internal class FakePlanRepository(
    private val plans: Map<ReadingPlanType, List<WeekPlanModel>>,
    startDate: LocalDate?,
    selectedReadingPlan: ReadingPlanType,
) : PlanRepository {
    val startDate = MutableStateFlow(startDate)
    val selectedReadingPlan = MutableStateFlow(selectedReadingPlan)
    val startTimestamps = mutableListOf<Long>()
    val seededTimestamps = mutableListOf<Long>()

    override suspend fun getPlans(readingPlanType: ReadingPlanType): List<WeekPlanModel> =
        plans[readingPlanType].orEmpty()

    override suspend fun setStartPlanTimestamp(timestamp: Long) {
        startTimestamps += timestamp
    }

    override fun getStartPlanTimestamp(): Flow<LocalDate?> = startDate

    override fun getSelectedReadingPlanFlow(): Flow<ReadingPlanType> = selectedReadingPlan

    override suspend fun setSelectedReadingPlan(readingPlanType: ReadingPlanType) {
        selectedReadingPlan.value = readingPlanType
    }

    override suspend fun seedDefaultStartDate(timestamp: Long) {
        seededTimestamps += timestamp
    }
}
