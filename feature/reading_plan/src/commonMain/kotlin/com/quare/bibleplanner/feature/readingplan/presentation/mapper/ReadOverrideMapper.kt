package com.quare.bibleplanner.feature.readingplan.presentation.mapper

import com.quare.bibleplanner.core.model.plan.WeekPlanModel

internal fun List<WeekPlanModel>.applyReadOverride(
    weekNumber: Int,
    dayNumber: Int,
    isRead: Boolean,
): List<WeekPlanModel> = map { week ->
    if (week.number != weekNumber) {
        week
    } else {
        week.copy(
            days = week.days.map { day ->
                if (day.number != dayNumber) {
                    day
                } else {
                    day.copy(
                        isRead = isRead,
                        passages = day.passages.map { it.copy(isRead = isRead) },
                    )
                }
            },
        )
    }
}
