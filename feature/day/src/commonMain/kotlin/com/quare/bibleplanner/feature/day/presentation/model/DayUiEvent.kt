package com.quare.bibleplanner.feature.day.presentation.model

import com.quare.bibleplanner.feature.day.domain.model.ChapterClickStrategy
import com.quare.bibleplanner.feature.day.domain.model.UpdateReadStatusOfPassageStrategy

internal sealed interface DayUiEvent {
    data class OnChapterCheckboxClick(
        val strategy: UpdateReadStatusOfPassageStrategy,
    ) : DayUiEvent

    data class OnChapterClick(
        val strategy: ChapterClickStrategy,
    ) : DayUiEvent

    data object OnDayReadToggle : DayUiEvent

    data class OnEditReadDate(
        val hour: Int,
        val minute: Int,
    ) : DayUiEvent

    data object OnEditDateClick : DayUiEvent

    data object OnShowTimePicker : DayUiEvent

    data object OnDismissPicker : DayUiEvent

    data class OnDateSelected(
        val utcDateMillis: Long,
    ) : DayUiEvent

    data class OnNotesChanged(
        val notes: String,
    ) : DayUiEvent

    data object OnNotesClear : DayUiEvent

    data object OnNotesFocus : DayUiEvent

    data object OnBackClick : DayUiEvent
}
