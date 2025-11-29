package com.quare.bibleplanner.feature.day.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class GetFinalTimestampAfterEditionUseCase {
    /**
     * Calculates the final timestamp by combining a local date with a duration.
     * The duration is added to the start of the day in the current system timezone.
     *
     * @param selectedLocalDate The local date to use as the base
     * @param eventDuration The duration to add to the start of the day
     * @return The final timestamp in milliseconds
     */
    operator fun invoke(
        selectedLocalDate: LocalDate,
        eventDuration: Duration,
    ): Long {
        val timeZone = TimeZone.currentSystemDefault()
        val startOfDay = selectedLocalDate.atStartOfDayIn(timeZone)
        val finalInstant = startOfDay + eventDuration
        return finalInstant.toEpochMilliseconds()
    }
}

