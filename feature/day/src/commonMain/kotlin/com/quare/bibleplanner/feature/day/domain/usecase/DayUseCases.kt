package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.plan.domain.usecase.UpdateDayReadStatusUseCase

internal data class DayUseCases(
    val updateDayReadStatus: UpdateDayReadStatusUseCase,
    val toggleChapterReadStatus: ToggleChapterReadStatusUseCase,
    val convertUtcDateToLocalDate: ConvertUtcDateToLocalDateUseCase,
    val updateDayReadTimestampWithDateAndTime: UpdateDayReadTimestampWithDateAndTimeUseCase,
)
