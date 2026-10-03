package com.quare.bibleplanner.core.date

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class ConvertUtcDateToLocalDateUseCase {
    /**
     * Converts a UTC timestamp (in milliseconds) to a LocalDate.
     * The date components are extracted from the UTC timestamp.
     *
     * @param utcDateMillis The UTC timestamp in milliseconds
     * @return The LocalDate extracted from the UTC timestamp
     */
    operator fun invoke(utcDateMillis: Long): LocalDate = Instant
        .fromEpochMilliseconds(utcDateMillis)
        .toLocalDateTime(TimeZone.UTC)
        .toLocalDate()
}
