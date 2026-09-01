package com.quare.bibleplanner.core.plan.domain.model

import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel

internal data class ScheduledPlanDayModel(
    val location: PlanDayLocationModel,
    val day: DayModel,
)
