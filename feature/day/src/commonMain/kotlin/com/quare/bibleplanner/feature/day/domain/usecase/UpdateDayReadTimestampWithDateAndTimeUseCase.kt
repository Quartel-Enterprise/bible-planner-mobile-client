package com.quare.bibleplanner.feature.day.domain.usecase

import kotlinx.datetime.LocalDate
import kotlin.time.Duration

internal class UpdateDayReadTimestampWithDateAndTimeUseCase(
    private val getFinalTimestampAfterEdition: GetFinalTimestampAfterEditionUseCase,
    private val updateDayReadTimestamp: UpdateDayReadTimestampUseCase,
) {
    /**
     * Calculates the final timestamp from a local date and duration, then updates the day's read timestamp.
     *
     * @param weekNumber The week number
     * @param dayNumber The day number
     * @param selectedLocalDate The local date to use as the base
     * @param eventDuration The duration to add to the start of the day
     */
    suspend operator fun invoke(
        weekNumber: Int,
        dayNumber: Int,
        selectedLocalDate: LocalDate,
        eventDuration: Duration,
    ) {
        updateDayReadTimestamp(
            weekNumber = weekNumber,
            dayNumber = dayNumber,
            readTimestamp = getFinalTimestampAfterEdition(
                selectedLocalDate = selectedLocalDate,
                eventDuration = eventDuration,
            ),
        )
    }
}
