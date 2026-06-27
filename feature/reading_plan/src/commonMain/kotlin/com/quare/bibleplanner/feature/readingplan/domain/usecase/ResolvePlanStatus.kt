package com.quare.bibleplanner.feature.readingplan.domain.usecase

import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanStatus

internal fun interface ResolvePlanStatus {
    operator fun invoke(weeks: List<WeekPlanModel>): PlanStatus
}
