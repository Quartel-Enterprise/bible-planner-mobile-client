package com.quare.bibleplanner.feature.readingplan.domain.usecase

import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage

internal fun interface ResolveOverallProgressMotivation {
    operator fun invoke(progress: Float): PlanMotivationMessage.OverallProgress
}
