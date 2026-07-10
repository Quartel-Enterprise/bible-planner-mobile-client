package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.model.plan.PlansModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent

class TrackReadingCompletionEventsUseCase(
    private val trackEvent: TrackEvent,
) {
    operator fun invoke(
        before: PlansModel,
        after: PlansModel,
        readingPlanType: ReadingPlanType,
        weekNumber: Int,
    ) {
        trackWeekCompletedIfNeeded(
            before = before.weeksFor(readingPlanType),
            after = after.weeksFor(readingPlanType),
            readingPlanType = readingPlanType,
            weekNumber = weekNumber,
        )
        ReadingPlanType.entries.forEach { planType ->
            trackPlanCompletedIfNeeded(
                before = before.weeksFor(planType),
                after = after.weeksFor(planType),
                planType = planType,
            )
        }
    }

    private fun trackWeekCompletedIfNeeded(
        before: List<WeekPlanModel>,
        after: List<WeekPlanModel>,
        readingPlanType: ReadingPlanType,
        weekNumber: Int,
    ) {
        val wasComplete = before.find { it.number == weekNumber }.isComplete()
        val isComplete = after.find { it.number == weekNumber }.isComplete()
        if (!wasComplete && isComplete) {
            trackEvent(
                name = AnalyticsEventNames.WEEK_COMPLETED,
                params = mapOf(
                    AnalyticsParams.PLAN_TYPE to readingPlanType.toAnalyticsValue(),
                    AnalyticsParams.WEEK_NUMBER to weekNumber,
                ),
            )
        }
    }

    private fun trackPlanCompletedIfNeeded(
        before: List<WeekPlanModel>,
        after: List<WeekPlanModel>,
        planType: ReadingPlanType,
    ) {
        if (!before.isPlanComplete() && after.isPlanComplete()) {
            trackEvent(
                name = AnalyticsEventNames.PLAN_COMPLETED,
                params = mapOf(AnalyticsParams.PLAN_TYPE to planType.toAnalyticsValue()),
            )
        }
    }

    private fun PlansModel.weeksFor(planType: ReadingPlanType): List<WeekPlanModel> = when (planType) {
        ReadingPlanType.CHRONOLOGICAL -> chronologicalOrder
        ReadingPlanType.BOOKS -> booksOrder
    }

    private fun WeekPlanModel?.isComplete(): Boolean = this != null && days.isNotEmpty() && days.all { it.isRead }

    private fun List<WeekPlanModel>.isPlanComplete(): Boolean = isNotEmpty() && all { it.isComplete() }

    private fun ReadingPlanType.toAnalyticsValue(): String = name.lowercase()
}
