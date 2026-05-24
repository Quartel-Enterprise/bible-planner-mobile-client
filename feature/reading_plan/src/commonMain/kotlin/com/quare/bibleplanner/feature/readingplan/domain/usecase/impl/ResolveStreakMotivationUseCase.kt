package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage.Streak
import com.quare.bibleplanner.feature.readingplan.domain.usecase.ResolveStreakMotivation
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

internal class ResolveStreakMotivationUseCase(
    private val localDateTimeProvider: LocalDateTimeProvider,
) : ResolveStreakMotivation {
    override fun invoke(
        days: List<DayModel>,
        today: LocalDate,
    ): Streak? {
        val readDates = days
            .mapNotNull { it.readTimestamp }
            .map { localDateTimeProvider.getLocalDateTime(it).date }
            .distinct()
            .sortedDescending()

        if (readDates.firstOrNull() != today) return null

        var count = 1
        var expected = today.minus(1, DateTimeUnit.DAY)
        for (date in readDates.drop(1)) {
            if (date != expected) break
            count++
            expected = expected.minus(1, DateTimeUnit.DAY)
        }

        return when {
            count >= 100 -> Streak.Day100
            count >= 30 -> Streak.Day30
            count >= 14 -> Streak.Day14
            count >= 7 -> Streak.Day7
            count >= 3 -> Streak.Day3
            count >= 1 -> Streak.Day1
            else -> null
        }
    }
}
