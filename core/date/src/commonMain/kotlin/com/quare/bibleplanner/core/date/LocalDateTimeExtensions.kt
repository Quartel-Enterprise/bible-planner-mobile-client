package com.quare.bibleplanner.core.date

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

/**
 * Converts a LocalDateTime to a LocalDate by extracting the date components.
 */
fun LocalDateTime.toLocalDate(): LocalDate = LocalDate(
    year = year,
    month = month,
    day = day,
)

fun LocalDate.toTimestampUTC(): Long = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
