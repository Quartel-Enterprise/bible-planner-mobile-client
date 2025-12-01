package com.quare.bibleplanner.feature.readingplan.domain.usecase

import com.quare.bibleplanner.core.model.plan.ReadingPlanType

fun interface SetSelectedReadingPlan {
    suspend operator fun invoke(readingPlanType: ReadingPlanType)
}
