package com.quare.bibleplanner.feature.releasenotes.presentation.utils

import com.quare.bibleplanner.feature.releasenotes.domain.model.ReleaseNoteModel
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesTab
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiState
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseVersionType
import com.quare.bibleplanner.feature.releasenotes.presentation.model.VisibleReleaseNote

fun ReleaseNotesUiState.Success.toVisibleNotes(): List<VisibleReleaseNote> = when (currentTab) {
    ReleaseNotesTab.LATEST -> listOfNotNull(latestRelease).toVisibleNotes(ReleaseVersionType.LATEST)
    ReleaseNotesTab.UPCOMING -> upcomingReleases.toVisibleNotes(ReleaseVersionType.UPCOMING)
    ReleaseNotesTab.PAST_VERSIONS -> pastReleases.toVisibleNotes(ReleaseVersionType.PAST)
}

private fun List<ReleaseNoteModel>.toVisibleNotes(type: ReleaseVersionType): List<VisibleReleaseNote> = map { note ->
    VisibleReleaseNote(
        note = note,
        type = type,
    )
}
