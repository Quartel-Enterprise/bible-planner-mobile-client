package com.quare.bibleplanner.feature.addnotesfreewarning.presentation.model

internal sealed interface AddNotesFreeWarningUiAction {
    data object NavigateBack : AddNotesFreeWarningUiAction

    data object NavigateToPro : AddNotesFreeWarningUiAction
}
