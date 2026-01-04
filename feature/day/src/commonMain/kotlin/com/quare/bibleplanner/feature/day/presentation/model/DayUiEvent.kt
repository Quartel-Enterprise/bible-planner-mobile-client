package com.quare.bibleplanner.feature.day.presentation.model

internal sealed interface DayUiEvent {
    data class OnChapterToggle(
        val passageIndex: Int,
        val chapterIndex: Int,
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
