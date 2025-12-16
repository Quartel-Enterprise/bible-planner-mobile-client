package com.quare.bibleplanner.feature.day.presentation.model

import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.plan.DayModel

internal sealed interface DayUiState {
    data object Loading : DayUiState

    data class Loaded(
        val day: DayModel,
        val weekNumber: Int,
        val books: List<BookDataModel>,
        val datePickerUiState: DatePickerUiState,
        val formattedReadDate: String?,
        val chapterReadStatus: Map<Pair<Int, Int>, Boolean>,
        val completedPassagesCount: Int,
        val totalPassagesCount: Int,
    ) : DayUiState
}
