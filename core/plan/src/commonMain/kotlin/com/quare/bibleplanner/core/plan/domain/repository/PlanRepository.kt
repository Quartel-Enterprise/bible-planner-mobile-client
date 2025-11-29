package com.quare.bibleplanner.core.plan.domain.repository

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel

interface PlanRepository {
    suspend fun getPlans(readingPlanType: ReadingPlanType): List<WeekPlanModel>
}
