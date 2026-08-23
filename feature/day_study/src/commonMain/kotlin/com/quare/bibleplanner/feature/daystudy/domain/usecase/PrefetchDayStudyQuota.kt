package com.quare.bibleplanner.feature.daystudy.domain.usecase

import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel

fun interface PrefetchDayStudyQuota {
    suspend operator fun invoke(day: PlanDayLocationModel)
}
