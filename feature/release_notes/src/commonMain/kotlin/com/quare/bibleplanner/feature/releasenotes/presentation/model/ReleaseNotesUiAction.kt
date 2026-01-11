package com.quare.bibleplanner.feature.releasenotes.presentation.model

sealed interface ReleaseNotesUiAction {
    data object NavigateBack : ReleaseNotesUiAction

    data class OpenUrl(
        val url: String,
    ) : ReleaseNotesUiAction
}
