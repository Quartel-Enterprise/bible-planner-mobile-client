package com.quare.bibleplanner.feature.releasenotes.presentation.factory

import com.quare.bibleplanner.feature.releasenotes.domain.usecase.GetReleaseNotesUseCase
import com.quare.bibleplanner.feature.releasenotes.domain.utils.VersionComparator
import com.quare.bibleplanner.feature.releasenotes.generated.ReleaseNotesBuildKonfig
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesTab
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiState

class ReleaseNotesUiStateFactory(
    private val getReleaseNotesUseCase: GetReleaseNotesUseCase,
) {
    private val appVersion = ReleaseNotesBuildKonfig.APP_VERSION

    fun createInitialState(): ReleaseNotesUiState = ReleaseNotesUiState.Loading

    suspend fun create(): ReleaseNotesUiState = getReleaseNotesUseCase()
        .fold(
            onSuccess = { notes ->
                val upcoming = notes.filter { note ->
                    VersionComparator.compare(note.version, appVersion) > 0
                }

                val publishedNotes = notes.filter { note ->
                    note.dateRepresentation != null && VersionComparator.compare(note.version, appVersion) <= 0
                }

                val latest = publishedNotes.firstOrNull()
                val past = publishedNotes.drop(1)

                val tabs = ReleaseNotesTab.entries.filter { tab ->
                    tab != ReleaseNotesTab.UPCOMING || upcoming.isNotEmpty()
                }

                ReleaseNotesUiState.Success(
                    latestRelease = latest,
                    upcomingReleases = upcoming,
                    pastReleases = past,
                    availableTabs = tabs,
                    currentTab = ReleaseNotesTab.LATEST,
                )
            },
            onFailure = {
                ReleaseNotesUiState.Error
            },
        )
}
