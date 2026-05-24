package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage.OverallProgress
import com.quare.bibleplanner.feature.readingplan.domain.usecase.ResolveOverallProgressMotivation
import kotlin.math.roundToInt

internal class ResolveOverallProgressMotivationUseCase : ResolveOverallProgressMotivation {
    override fun invoke(progress: Float): OverallProgress {
        val pct = progress.roundToInt().coerceIn(0, 100)
        return when (pct) {
            0 -> OverallProgress.Zero
            in 1..5 -> OverallProgress.EarlyStart
            in 6..15 -> OverallProgress.BuildingSolid
            in 16..30 -> OverallProgress.ApproachingThird
            in 31..49 -> OverallProgress.PastThirty
            50 -> OverallProgress.Halfway
            in 51..74 -> OverallProgress.MoreThanHalf
            75 -> OverallProgress.ThreeQuarters
            in 76..90 -> OverallProgress.FinalStretch
            in 91..99 -> OverallProgress.AlmostThere
            else -> OverallProgress.Completed
        }
    }
}
