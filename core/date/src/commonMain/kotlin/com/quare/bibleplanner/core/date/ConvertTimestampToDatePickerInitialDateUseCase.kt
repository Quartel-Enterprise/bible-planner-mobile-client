package com.quare.bibleplanner.core.date

import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class ConvertTimestampToDatePickerInitialDateUseCase {
    operator fun invoke(timestamp: Long): Long {
        val localTimeZone = TimeZone.currentSystemDefault()

        val localDateTime = Instant
            .fromEpochMilliseconds(timestamp)
            .toLocalDateTime(localTimeZone)

        val localDate = localDateTime.toLocalDate()

        val localMidnight = localDate.atStartOfDayIn(localTimeZone)
        return localMidnight.toEpochMilliseconds()
    }
}
