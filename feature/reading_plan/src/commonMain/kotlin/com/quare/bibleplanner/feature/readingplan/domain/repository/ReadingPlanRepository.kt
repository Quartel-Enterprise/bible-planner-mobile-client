package com.quare.bibleplanner.feature.readingplan.domain.repository

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import kotlinx.coroutines.flow.Flow

interface ReadingPlanRepository {
    fun getSelectedReadingPlanFlow(): Flow<ReadingPlanType>

    suspend fun setSelectedReadingPlan(readingPlanType: ReadingPlanType)
}
