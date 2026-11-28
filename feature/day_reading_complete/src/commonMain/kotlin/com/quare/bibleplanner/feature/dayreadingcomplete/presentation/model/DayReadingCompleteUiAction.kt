package com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model

sealed interface DayReadingCompleteUiAction {
    data class ShowSnackBar(
        val message: DayReadingCompleteMessage,
    ) : DayReadingCompleteUiAction
}
