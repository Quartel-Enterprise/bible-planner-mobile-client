package com.quare.bibleplanner.feature.day.domain.model

import com.quare.bibleplanner.core.plan.domain.usecase.DeleteDayNotesUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetMaxFreeNotesAmountUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.ReadDayToggleOperationUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.UpdateDayNotesUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.ConvertUtcDateToLocalDateUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.ShouldBlockAddNotesUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.ToggleChapterReadStatusUseCase
import com.quare.bibleplanner.feature.day.domain.usecase.UpdateDayReadTimestampWithDateAndTimeUseCase

internal data class DayUseCases(
    val readDayToggleOperation: ReadDayToggleOperationUseCase,
    val toggleChapterReadStatus: ToggleChapterReadStatusUseCase,
    val convertUtcDateToLocalDate: ConvertUtcDateToLocalDateUseCase,
    val updateDayReadTimestampWithDateAndTime: UpdateDayReadTimestampWithDateAndTimeUseCase,
    val updateDayNotes: UpdateDayNotesUseCase,
    val shouldBlockAddNotes: ShouldBlockAddNotesUseCase,
    val getMaxFreeNotesAmount: GetMaxFreeNotesAmountUseCase,
    val deleteDayNotes: DeleteDayNotesUseCase
)
