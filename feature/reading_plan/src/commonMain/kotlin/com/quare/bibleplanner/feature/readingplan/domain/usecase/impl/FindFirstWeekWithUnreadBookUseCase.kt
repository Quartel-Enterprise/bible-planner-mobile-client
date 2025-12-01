package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.feature.readingplan.domain.usecase.FindFirstWeekWithUnreadBook

internal class FindFirstWeekWithUnreadBookUseCase : FindFirstWeekWithUnreadBook {
    override operator fun invoke(weeks: List<WeekPlanModel>): Int? = weeks
        .find { week ->
            week.days.any { day ->
                day.passages.any { passage ->
                    !passage.isRead
                }
            }
        }?.number
}
