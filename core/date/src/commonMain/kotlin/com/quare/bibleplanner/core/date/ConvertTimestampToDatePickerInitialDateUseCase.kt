package com.quare.bibleplanner.core.date

import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class ConvertTimestampToDatePickerInitialDateUseCase {
    // Why: the DatePicker expects a UTC timestamp, so the date is snapped to midnight in the
    // local timezone to make the picker show the correct local date.
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
