package com.quare.bibleplanner.feature.releasenotes.presentation.factory

import com.quare.bibleplanner.feature.releasenotes.domain.usecase.GetReleaseNotesUseCase
import com.quare.bibleplanner.feature.releasenotes.domain.utils.VersionComparator
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesTab
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiState

class ReleaseNotesUiStateFactory(
    private val getReleaseNotesUseCase: GetReleaseNotesUseCase,
) {
    companion object {
        private const val APP_VERSION = "1.8.0"
    }

    fun createInitialState(): ReleaseNotesUiState = ReleaseNotesUiState.Loading

    suspend fun create(): ReleaseNotesUiState = getReleaseNotesUseCase()
        .fold(
            onSuccess = { notes ->
                val sortedNotes = notes.sortedWith(VersionComparator).reversed()

                val upcoming = sortedNotes.filter { note ->
                    val isAhead = VersionComparator.compare(note.version, APP_VERSION) > 0
                    val isUnpublished = note.dateRepresentation == null
                    isAhead || isUnpublished
                }

                val publishedNotes = sortedNotes.filter { note ->
                    note.dateRepresentation != null && VersionComparator.compare(note.version, APP_VERSION) <= 0
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
