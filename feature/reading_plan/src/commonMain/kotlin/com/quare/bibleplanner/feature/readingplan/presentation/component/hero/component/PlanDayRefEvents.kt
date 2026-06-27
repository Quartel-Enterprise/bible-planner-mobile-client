package com.quare.bibleplanner.feature.readingplan.presentation.component.hero.component

import com.quare.bibleplanner.feature.readingplan.domain.model.PlanDayRef
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent

internal fun PlanDayRef.toDayClick(): ReadingPlanUiEvent = ReadingPlanUiEvent.OnDayClick(
    dayNumber = dayNumber,
    weekNumber = weekNumber,
)

internal fun PlanDayRef.toReadClick(): ReadingPlanUiEvent = ReadingPlanUiEvent.OnDayReadClick(
    dayNumber = dayNumber,
    weekNumber = weekNumber,
)
