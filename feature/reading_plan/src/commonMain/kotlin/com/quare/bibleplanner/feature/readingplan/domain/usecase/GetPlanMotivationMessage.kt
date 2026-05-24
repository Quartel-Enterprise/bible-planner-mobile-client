package com.quare.bibleplanner.feature.readingplan.domain.usecase

import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage

internal fun interface GetPlanMotivationMessage {
    operator fun invoke(
        weeks: List<WeekPlanModel>,
        bibleProgress: Float,
    ): PlanMotivationMessage
}
