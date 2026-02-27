package com.quare.bibleplanner.core.plan.domain.repository

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface PlanRepository {
    suspend fun getPlans(readingPlanType: ReadingPlanType): List<WeekPlanModel>

    suspend fun setStartPlanTimestamp(timestamp: Long)

    suspend fun deleteStartPlanTimestamp()

    fun getStartPlanTimestamp(): Flow<LocalDate?>

    fun getSelectedReadingPlanFlow(): Flow<ReadingPlanType>

    suspend fun setSelectedReadingPlan(readingPlanType: ReadingPlanType)
}
