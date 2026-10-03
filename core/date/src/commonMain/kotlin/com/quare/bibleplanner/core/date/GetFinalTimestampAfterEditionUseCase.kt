package com.quare.bibleplanner.core.date

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.Duration

class GetFinalTimestampAfterEditionUseCase {
    operator fun invoke(
        selectedLocalDate: LocalDate,
        eventDuration: Duration = Duration.ZERO,
    ): Long {
        val timeZone = TimeZone.currentSystemDefault()
        val startOfDay = selectedLocalDate.atStartOfDayIn(timeZone)
        val finalInstant = startOfDay + eventDuration
        return finalInstant.toEpochMilliseconds()
    }
}
