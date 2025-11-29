package com.quare.bibleplanner.feature.day.presentation.mapper

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class ReadDateFormatter {
    fun format(timestamp: Long): String {
        val localDateTime: LocalDateTime = Instant
            .fromEpochMilliseconds(timestamp)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        // Format: "26 Nov 2025, 22:47"
        val day = localDateTime.day
        val month = localDateTime.month.name.take(3) // First 3 letters of month
        val year = localDateTime.year
        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')
        return "$day $month $year, $hour:$minute"
    }
}

