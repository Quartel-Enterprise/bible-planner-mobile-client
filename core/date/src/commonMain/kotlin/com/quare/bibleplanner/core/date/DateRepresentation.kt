package com.quare.bibleplanner.core.date

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.periodUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

sealed interface DateRepresentation {
    data object Tomorrow : DateRepresentation

    data object Today : DateRepresentation

    data object Yesterday : DateRepresentation

    data class DaysAgo(
        val days: Int,
    ) : DateRepresentation

    data object LastWeek : DateRepresentation

    data class WeeksAgo(
        val weeks: Int,
    ) : DateRepresentation

    data object LastMonth : DateRepresentation

    data class MonthsAgo(
        val months: Int,
    ) : DateRepresentation

    data class Custom(
        val date: LocalDate,
    ) : DateRepresentation
}

fun LocalDate.toDateRepresentation(): DateRepresentation {
    val today = Clock.System
        .now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
    val tomorrow = today.plus(1, DateTimeUnit.DAY)
    val yesterday = today.minus(1, DateTimeUnit.DAY)

    return when {
        this == tomorrow -> {
            DateRepresentation.Tomorrow
        }

        this == today -> {
            DateRepresentation.Today
        }

        this == yesterday -> {
            DateRepresentation.Yesterday
        }

        this < yesterday -> {
            val period = this.periodUntil(today)
            val totalMonths = period.years * 12 + period.months
            val totalDays = period.days

            when {
                totalMonths > 1 -> DateRepresentation.MonthsAgo(totalMonths)
                totalMonths == 1 -> DateRepresentation.LastMonth
                totalDays >= 14 -> DateRepresentation.WeeksAgo(totalDays / 7)
                totalDays >= 7 -> DateRepresentation.LastWeek
                totalDays > 1 -> DateRepresentation.DaysAgo(totalDays)
                else -> DateRepresentation.Custom(this)
            }
        }

        else -> {
            DateRepresentation.Custom(this)
        }
    }
}
