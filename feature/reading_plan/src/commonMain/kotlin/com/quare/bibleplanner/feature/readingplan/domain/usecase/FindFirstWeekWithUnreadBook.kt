package com.quare.bibleplanner.feature.readingplan.domain.usecase

import com.quare.bibleplanner.core.model.plan.WeekPlanModel

fun interface FindFirstWeekWithUnreadBook {
    operator fun invoke(weeks: List<WeekPlanModel>): Int?
}
