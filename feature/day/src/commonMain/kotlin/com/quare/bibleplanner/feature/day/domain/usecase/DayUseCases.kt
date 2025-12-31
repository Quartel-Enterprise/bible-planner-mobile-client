package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.plan.domain.usecase.ReadDayToggleOperationUseCase

internal data class DayUseCases(
    val readDayToggleOperation: ReadDayToggleOperationUseCase,
    val toggleChapterReadStatus: ToggleChapterReadStatusUseCase,
    val convertUtcDateToLocalDate: ConvertUtcDateToLocalDateUseCase,
    val updateDayReadTimestampWithDateAndTime: UpdateDayReadTimestampWithDateAndTimeUseCase,
    val updateDayNotes: UpdateDayNotesUseCase,
)
