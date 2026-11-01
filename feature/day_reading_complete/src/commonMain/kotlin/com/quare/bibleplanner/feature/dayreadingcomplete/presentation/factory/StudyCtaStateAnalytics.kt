package com.quare.bibleplanner.feature.dayreadingcomplete.presentation.factory

import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.toPlanTypeAnalyticsValue
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.StudyCtaState
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiState

internal fun StudyCtaState.toAnalyticsValue(): String = when (this) {
    is StudyCtaState.FreeWithQuota -> "free"
    is StudyCtaState.FreeExhausted -> "free_exhausted"
    StudyCtaState.Pro -> "pro"
}

internal fun DayReadingCompleteUiState.Loaded.toShownParams(
    day: PlanDayLocationModel,
    ctaState: StudyCtaState,
): Map<String, Any> = mapOf(
    AnalyticsParams.PLAN_TYPE to day.readingPlanType.name.toPlanTypeAnalyticsValue(),
    AnalyticsParams.WEEK_NUMBER to day.weekNumber,
    AnalyticsParams.DAY_NUMBER to day.dayNumber,
    AnalyticsParams.TIMING to timing.name.lowercase(),
    AnalyticsParams.ACCOUNT_STATE to ctaState.toAnalyticsValue(),
    AnalyticsParams.CHAPTER_COUNT to chapterCount,
)

internal fun StudyCtaState.toCtaClickedParams(source: String): Map<String, Any> = mapOf(
    AnalyticsParams.ACCOUNT_STATE to toAnalyticsValue(),
    AnalyticsParams.SOURCE to source,
)
