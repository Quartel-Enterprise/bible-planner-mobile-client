package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.utils.toLocalDate
import kotlinx.datetime.LocalDate
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
}
