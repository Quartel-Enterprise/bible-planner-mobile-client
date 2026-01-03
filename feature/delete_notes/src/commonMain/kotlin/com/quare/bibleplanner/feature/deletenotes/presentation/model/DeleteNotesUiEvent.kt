package com.quare.bibleplanner.feature.deletenotes.presentation.model

sealed interface DeleteNotesUiEvent {
    data object OnConfirmDelete : DeleteNotesUiEvent

    data object OnCancel : DeleteNotesUiEvent
}
