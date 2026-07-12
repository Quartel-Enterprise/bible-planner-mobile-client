package com.quare.bibleplanner.core.date

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

sealed interface RelativeTime {
    data object JustNow : RelativeTime

    data class MinutesAgo(
        val minutes: Int,
    ) : RelativeTime

    data class HoursAgo(
        val hours: Int,
    ) : RelativeTime

    data class OlderThanADay(
        val representation: DateRepresentation,
    ) : RelativeTime
}

fun Instant.toRelativeTime(now: Instant = Clock.System.now()): RelativeTime {
    val elapsed = now - this
    return when {
        elapsed < 1.minutes -> RelativeTime.JustNow

        elapsed < 1.hours -> RelativeTime.MinutesAgo(elapsed.inWholeMinutes.toInt())

        elapsed < 24.hours -> RelativeTime.HoursAgo(elapsed.inWholeHours.toInt())

        else -> RelativeTime.OlderThanADay(
            toLocalDateTime(TimeZone.currentSystemDefault()).date.toDateRepresentation(),
        )
    }
}
