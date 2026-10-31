package com.quare.bibleplanner.feature.releasenotes.presentation.utils

import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesTab
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiState
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseVersionType
import com.quare.bibleplanner.feature.releasenotes.presentation.model.VisibleReleaseNote

fun ReleaseNotesUiState.Success.toVisibleNotes(): List<VisibleReleaseNote> = when (currentTab) {
    ReleaseNotesTab.LATEST -> listOfNotNull(latestRelease).map { note ->
        VisibleReleaseNote(
            note = note,
            type = ReleaseVersionType.LATEST,
        )
    }

    ReleaseNotesTab.UPCOMING -> upcomingReleases.map { note ->
        VisibleReleaseNote(
            note = note,
            type = ReleaseVersionType.UPCOMING,
        )
    }

    ReleaseNotesTab.PAST_VERSIONS -> pastReleases.map { note ->
        VisibleReleaseNote(
            note = note,
            type = ReleaseVersionType.PAST,
        )
    }
}
