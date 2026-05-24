package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage
import com.quare.bibleplanner.feature.readingplan.domain.usecase.GetPlanMotivationMessage
import com.quare.bibleplanner.feature.readingplan.domain.usecase.ResolveDaySituationMotivation
import com.quare.bibleplanner.feature.readingplan.domain.usecase.ResolveMilestoneMotivation
import com.quare.bibleplanner.feature.readingplan.domain.usecase.ResolveOverallProgressMotivation
import com.quare.bibleplanner.feature.readingplan.domain.usecase.ResolveStreakMotivation

internal class GetPlanMotivationMessageUseCase(
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val localDateTimeProvider: LocalDateTimeProvider,
    private val resolveMilestoneMotivation: ResolveMilestoneMotivation,
    private val resolveStreakMotivation: ResolveStreakMotivation,
    private val resolveDaySituationMotivation: ResolveDaySituationMotivation,
    private val resolveOverallProgressMotivation: ResolveOverallProgressMotivation,
) : GetPlanMotivationMessage {
    override fun invoke(
        weeks: List<WeekPlanModel>,
        bibleProgress: Float,
    ): PlanMotivationMessage {
        val nowMillis = currentTimestampProvider.getCurrentTimestamp()
        val today = localDateTimeProvider.getLocalDateTime(nowMillis).date
        val days = weeks.flatMap { it.days }

        return resolveMilestoneMotivation(days, nowMillis)
            ?: resolveStreakMotivation(days, today)
            ?: resolveDaySituationMotivation(days, today)
            ?: resolveOverallProgressMotivation(bibleProgress)
    }
}
