package com.quare.bibleplanner.feature.day.domain.usecase

internal data class DayUseCases(
    val getDayDetails: GetDayDetailsUseCase,
    val getBooks: GetBooksUseCase,
    val updateDayReadStatus: UpdateDayReadStatusUseCase,
    val updateChapterReadStatus: UpdateChapterReadStatusUseCase,
    val updateDayReadTimestamp: UpdateDayReadTimestampUseCase,
)

