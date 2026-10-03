package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.date.GetFinalTimestampAfterEditionUseCase
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import kotlinx.datetime.LocalDate
import kotlin.time.Duration

internal class UpdateDayReadTimestampWithDateAndTimeUseCase(
    private val getFinalTimestampAfterEdition: GetFinalTimestampAfterEditionUseCase,
    private val updateDayReadTimestamp: UpdateDayReadTimestampUseCase,
) {
    suspend operator fun invoke(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
        selectedLocalDate: LocalDate,
        eventDuration: Duration,
    ) {
        updateDayReadTimestamp(
            weekNumber = weekNumber,
            dayNumber = dayNumber,
            readingPlanType = readingPlanType,
            readTimestamp = getFinalTimestampAfterEdition(
                selectedLocalDate = selectedLocalDate,
                eventDuration = eventDuration,
            ),
        )
    }
}
