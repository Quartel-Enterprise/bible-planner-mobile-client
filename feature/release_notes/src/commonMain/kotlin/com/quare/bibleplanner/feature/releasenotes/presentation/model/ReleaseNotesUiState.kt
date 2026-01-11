package com.quare.bibleplanner.feature.releasenotes.presentation.model

import com.quare.bibleplanner.feature.releasenotes.domain.model.ReleaseNoteModel

sealed interface ReleaseNotesUiState {
    data object Loading : ReleaseNotesUiState

    data object Error : ReleaseNotesUiState

    data class Success(
        val latestRelease: ReleaseNoteModel?,
        val upcomingReleases: List<ReleaseNoteModel>,
        val pastReleases: List<ReleaseNoteModel>,
        val currentTab: ReleaseNotesTab,
        val availableTabs: List<ReleaseNotesTab>,
    ) : ReleaseNotesUiState
}
