package com.quare.bibleplanner.feature.addnotesfreewarning.presentation.model

sealed interface AddNotesFreeWarningUiEvent {
    data object OnSubscribeToPremium : AddNotesFreeWarningUiEvent

    data object OnCancel : AddNotesFreeWarningUiEvent
}
