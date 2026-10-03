package com.quare.bibleplanner.core.date

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class ConvertUtcDateToLocalDateUseCase {
    operator fun invoke(utcDateMillis: Long): LocalDate = Instant
        .fromEpochMilliseconds(utcDateMillis)
        .toLocalDateTime(TimeZone.UTC)
        .toLocalDate()
}
