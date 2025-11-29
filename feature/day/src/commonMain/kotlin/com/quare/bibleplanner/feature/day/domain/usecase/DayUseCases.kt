package com.quare.bibleplanner.feature.day.domain.usecase

internal data class DayUseCases(
    val updateDayReadStatus: UpdateDayReadStatusUseCase,
    val toggleChapterReadStatus: ToggleChapterReadStatusUseCase,
    val convertUtcDateToLocalDate: ConvertUtcDateToLocalDateUseCase,
    val updateDayReadTimestampWithDateAndTime: UpdateDayReadTimestampWithDateAndTimeUseCase,
)
