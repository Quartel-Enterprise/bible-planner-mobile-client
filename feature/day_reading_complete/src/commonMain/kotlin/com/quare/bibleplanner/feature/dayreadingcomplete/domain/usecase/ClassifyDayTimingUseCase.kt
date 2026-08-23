package com.quare.bibleplanner.feature.dayreadingcomplete.domain.usecase

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.DayTimingState
import kotlinx.datetime.LocalDate

class ClassifyDayTimingUseCase(
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val localDateTimeProvider: LocalDateTimeProvider,
) {
    operator fun invoke(plannedReadDate: LocalDate?): DayTimingState {
        if (plannedReadDate == null) return DayTimingState.ON_TIME
        val today = localDateTimeProvider
            .getLocalDateTime(currentTimestampProvider.getCurrentTimestamp())
            .date
        return when {
            plannedReadDate == today -> DayTimingState.ON_TIME
            plannedReadDate < today -> DayTimingState.OVERDUE
            else -> DayTimingState.EARLY
        }
    }
}
