package com.quare.bibleplanner.feature.readingplan.presentation.mapper

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.domain.getGlobalDayIndex
import com.quare.bibleplanner.feature.readingplan.presentation.model.DayPlanPresentationModel
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekGroup
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

        val todayGlobalIndex: Int? = weeks
            .asSequence()
            .flatMap { week -> week.days.asSequence().map { week.number to it } }
            .firstOrNull { (_, day) -> day.isToday }
            ?.let { (weekNumber, day) -> getGlobalDayIndex(weekNumber = weekNumber, dayNumber = day.number) }

        var currentAssigned = false
        return weeks.map { week ->
            val group = when {
                week.days.all { it.isRead } -> WeekGroup.Completed

                !currentAssigned -> {
                    currentAssigned = true
                    WeekGroup.Current
                }

                else -> WeekGroup.Upcoming
            }
            WeekPlanPresentationModel(
                weekPlan = week,
                dayPlans = week.days.map { day ->
                    val globalDayIndex = getGlobalDayIndex(weekNumber = week.number, dayNumber = day.number)
                    val isNextToRead = nextToReadKey == (week.number to day.number)
                    val isActive = day.isToday && !day.isRead
                    DayPlanPresentationModel(
                        day = day,
                        globalDayIndex = globalDayIndex,
                        shouldShowYear = day.plannedReadDate?.year != currentYear,
                        isNextToRead = isNextToRead,
                        isOverdue = !day.isRead &&
                            todayGlobalIndex != null &&
                            globalDayIndex < todayGlobalIndex,
                        isActive = isActive,
                        isAccented = isActive || isNextToRead,
                    )
                },
                group = group,
                isExpanded = expandedWeeks.contains(week.number),
                readDaysCount = week.days.count { it.isRead },
                totalDays = week.days.size,
            )
        }
    }
}
