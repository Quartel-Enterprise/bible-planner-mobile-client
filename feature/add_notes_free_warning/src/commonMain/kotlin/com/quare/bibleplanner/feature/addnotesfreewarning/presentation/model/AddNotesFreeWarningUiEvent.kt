package com.quare.bibleplanner.feature.addnotesfreewarning.presentation.model

sealed interface AddNotesFreeWarningUiEvent {
    data object OnSubscribeToPro : AddNotesFreeWarningUiEvent

    data object OnCancel : AddNotesFreeWarningUiEvent
}
