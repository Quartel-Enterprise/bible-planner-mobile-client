package com.quare.bibleplanner.feature.readingplan.presentation.mapper

import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekPlanPresentationModel

internal class CalculateIsFirstUnreadWeekVisible {
    operator fun invoke(
        weekPlans: List<WeekPlanPresentationModel>,
        isScrolledDown: Boolean,
    ): Boolean {
        val firstUnreadWeek = weekPlans.find { week ->
            week.weekPlan.days.any { day ->
                day.passages.any { passage -> !passage.isRead }
            }
        }
        // If the first unread week is expanded and we're at the top, we're "in" it
        return firstUnreadWeek != null && firstUnreadWeek.isExpanded && !isScrolledDown
    }
}
