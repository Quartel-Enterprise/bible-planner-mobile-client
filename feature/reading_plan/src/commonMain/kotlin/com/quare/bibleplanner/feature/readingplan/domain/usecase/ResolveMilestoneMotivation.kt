package com.quare.bibleplanner.feature.readingplan.domain.usecase

import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage

internal fun interface ResolveMilestoneMotivation {
    operator fun invoke(
        days: List<DayModel>,
        nowMillis: Long,
    ): PlanMotivationMessage.Milestone?
}
