package com.quare.bibleplanner.feature.editplanstartdate.domain.usecase

import com.quare.bibleplanner.core.date.toLocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal class ConvertTimestampToDatePickerInitialDateUseCase {
    /**
     * Converts a timestamp to a UTC timestamp at midnight (local time) for the date picker.
     * The DatePicker expects a UTC timestamp, but we need to ensure it represents
     * midnight in the local timezone so the correct date is displayed.
     *
     * @param timestamp The timestamp in milliseconds (can be any time of day)
     * @return A UTC timestamp representing midnight in local timezone for the date
     */
    operator fun invoke(timestamp: Long): Long {
        // Get the local timezone once
        val localTimeZone = TimeZone.currentSystemDefault()

        // Convert timestamp to LocalDateTime in local timezone to get the date
        val localDateTime = Instant
            .fromEpochMilliseconds(timestamp)
            .toLocalDateTime(localTimeZone)

        // Extract the date components
        val localDate = localDateTime.toLocalDate()

        // Convert the local date to midnight in LOCAL timezone, then get UTC timestamp
        // This ensures the DatePicker shows the correct local date
        val localMidnight = localDate.atStartOfDayIn(localTimeZone)
        return localMidnight.toEpochMilliseconds()
    }
}

