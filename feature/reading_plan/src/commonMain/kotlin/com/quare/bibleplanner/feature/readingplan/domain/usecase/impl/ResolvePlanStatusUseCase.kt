package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.domain.getGlobalDayIndex
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanDayRef
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMode
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanStatus
import com.quare.bibleplanner.feature.readingplan.domain.usecase.ResolvePlanStatus

internal class ResolvePlanStatusUseCase : ResolvePlanStatus {
    override fun invoke(weeks: List<WeekPlanModel>): PlanStatus {
        val indexedDays = weeks.flatMap { week ->
            week.days.map { day ->
                IndexedDay(
                    day = day,
                    weekNumber = week.number,
                    globalIndex = getGlobalDayIndex(weekNumber = week.number, dayNumber = day.number),
                )
            }
        }
        val nextDay = indexedDays.firstOrNull { !it.day.isRead }
        val todayDay = indexedDays.firstOrNull { it.day.isToday }
        val readDays = indexedDays.count { it.day.isRead }
        val lastReadGlobalIndex = indexedDays
            .filter { it.day.isRead }
            .maxOfOrNull { it.globalIndex }
            ?: 0
        val todayGlobalIndex = todayDay?.globalIndex
        return PlanStatus(
            mode = resolveMode(
                nextDay = nextDay,
                todayGlobalIndex = todayGlobalIndex,
                readDays = readDays,
            ),
            nextDay = nextDay?.toRef(),
            todayDay = todayDay?.toRef(),
            totalDays = indexedDays.size,
            readDays = readDays,
            streakDays = calculateStreak(indexedDays),
            daysAhead = diffOrZero(todayGlobalIndex, lastReadGlobalIndex),
            daysBehind = diffOrZero(todayGlobalIndex, nextDay?.globalIndex, reversed = true),
            daysSinceLastRead = diffOrZero(todayGlobalIndex, lastReadGlobalIndex, reversed = true)
                .takeIf { it > 0 },
        )
    }

    private fun resolveMode(
        nextDay: IndexedDay?,
        todayGlobalIndex: Int?,
        readDays: Int,
    ): PlanMode = when {
        nextDay == null -> PlanMode.Done
        todayGlobalIndex == null -> if (readDays == 0) PlanMode.New else PlanMode.OnTrack
        nextDay.globalIndex < todayGlobalIndex -> PlanMode.Behind
        nextDay.globalIndex == todayGlobalIndex -> if (readDays == 0) PlanMode.New else PlanMode.OnTrack
        nextDay.globalIndex == todayGlobalIndex + 1 -> PlanMode.CaughtUp
        else -> PlanMode.Ahead
    }

    private fun diffOrZero(
        todayGlobalIndex: Int?,
        otherGlobalIndex: Int?,
        reversed: Boolean = false,
    ): Int {
        if (todayGlobalIndex == null || otherGlobalIndex == null) return 0
        val diff = if (reversed) todayGlobalIndex - otherGlobalIndex else otherGlobalIndex - todayGlobalIndex
        return diff.coerceAtLeast(0)
    }

    private fun calculateStreak(indexedDays: List<IndexedDay>): Int {
        val firstUnreadIndex = indexedDays.indexOfFirst { !it.day.isRead }
        val lastReadIndex = if (firstUnreadIndex == -1) indexedDays.lastIndex else firstUnreadIndex - 1
        var streak = 0
        for (index in lastReadIndex downTo 0) {
            if (!indexedDays[index].day.isRead) break
            streak++
        }
        return streak
    }

    private fun IndexedDay.toRef(): PlanDayRef = PlanDayRef(
        weekNumber = weekNumber,
        dayNumber = day.number,
        globalIndex = globalIndex,
        passages = day.passages,
        plannedReadDate = day.plannedReadDate,
    )

    private class IndexedDay(
        val day: DayModel,
        val weekNumber: Int,
        val globalIndex: Int,
    )
}
