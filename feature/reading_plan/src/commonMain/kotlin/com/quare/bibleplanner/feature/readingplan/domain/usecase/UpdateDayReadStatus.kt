package com.quare.bibleplanner.feature.readingplan.domain.usecase

import com.quare.bibleplanner.core.model.plan.ReadingPlanType

internal fun interface UpdateDayReadStatus {
    suspend operator fun invoke(
        weekNumber: Int,
        dayNumber: Int,
        isRead: Boolean,
        readingPlanType: ReadingPlanType,
    )
}
