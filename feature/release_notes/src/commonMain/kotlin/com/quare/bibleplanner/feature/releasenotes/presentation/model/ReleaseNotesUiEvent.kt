package com.quare.bibleplanner.feature.releasenotes.presentation.model

sealed interface ReleaseNotesUiEvent {
    data class OnTabSelected(
        val tab: ReleaseNotesTab,
    ) : ReleaseNotesUiEvent

    data object OnBackClicked : ReleaseNotesUiEvent

    data object OnGithubAllReleasesClicked : ReleaseNotesUiEvent

    data class OnGithubVersionClicked(
        val version: String,
    ) : ReleaseNotesUiEvent
}
