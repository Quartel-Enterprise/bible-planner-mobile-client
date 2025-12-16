package com.quare.bibleplanner.feature.readingplan.presentation.mapper

import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekPlanPresentationModel

internal class WeeksPlanPresentationMapper {
    fun map(
        weeks: List<WeekPlanModel>,
        expandedWeeks: Set<Int>,
    ): List<WeekPlanPresentationModel> = weeks.map { week ->
        WeekPlanPresentationModel(
            weekPlan = week,
            isExpanded = expandedWeeks.contains(week.number),
            readDaysCount = week.days.count { it.isRead },
            totalDays = week.days.size,
        )
    }
}
