package com.quare.bibleplanner.feature.readingplan.presentation.mapper

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.feature.readingplan.presentation.model.DayPlanPresentationModel
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekPlanPresentationModel

internal class WeeksPlanPresentationMapper(
    localDateTimeProvider: LocalDateTimeProvider,
    currentTimestampProvider: CurrentTimestampProvider,
) {
    private val currentYear = localDateTimeProvider
        .getLocalDateTime(
            currentTimestampProvider.getCurrentTimestamp(),
        ).year

    fun map(
        weeks: List<WeekPlanModel>,
        expandedWeeks: Set<Int>,
    ): List<WeekPlanPresentationModel> {
        val nextToReadKey: Pair<Int, Int>? = weeks
            .asSequence()
            .flatMap { week -> week.days.asSequence().map { week.number to it } }
            .firstOrNull { (_, day) -> !day.isRead }
            ?.let { (weekNumber, day) -> weekNumber to day.number }

        return weeks.map { week ->
            WeekPlanPresentationModel(
                weekPlan = week,
                dayPlans = week.days.map { day ->
                    DayPlanPresentationModel(
                        day = day,
                        shouldShowYear = day.plannedReadDate?.year != currentYear,
                        isNextToRead = nextToReadKey == (week.number to day.number),
                    )
                },
                isExpanded = expandedWeeks.contains(week.number),
                readDaysCount = week.days.count { it.isRead },
                totalDays = week.days.size,
            )
        }
    }
}
