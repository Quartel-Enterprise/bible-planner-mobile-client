package com.quare.bibleplanner.feature.day.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal class ConvertUtcDateToLocalDateUseCase {
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

    private fun LocalDateTime.toLocalDate() = LocalDate(
        year = year,
        month = month,
        day = day
    )
}

