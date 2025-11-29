package com.quare.bibleplanner.core.plan.data.repository

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.data.datasource.PlanLocalDataSource
import com.quare.bibleplanner.core.plan.data.mapper.WeekPlanDtoToModelMapper
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository

class PlanRepositoryImpl(
    private val planLocalDataSource: PlanLocalDataSource,
    private val weekPlanDtoToModelMapper: WeekPlanDtoToModelMapper,
) : PlanRepository {
    override suspend fun getPlans(readingPlanType: ReadingPlanType): List<WeekPlanModel> = planLocalDataSource
        .getPlans(readingPlanType)
        .map(weekPlanDtoToModelMapper::map)
}
