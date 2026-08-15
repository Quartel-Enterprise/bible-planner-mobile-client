package com.quare.bibleplanner.feature.verse.addnote.presentation.model

import org.jetbrains.compose.resources.StringResource

sealed interface VerseNoteUiAction {
    data object NavigateBack : VerseNoteUiAction

    data class NavigateBackWithMessage(
        val stringResource: StringResource,
    ) : VerseNoteUiAction
}
