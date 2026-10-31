package com.quare.bibleplanner.feature.releasenotes.presentation.utils

import bibleplanner.feature.release_notes.generated.resources.Res
import bibleplanner.feature.release_notes.generated.resources.release_notes_tab_latest
import bibleplanner.feature.release_notes.generated.resources.release_notes_tab_past
import bibleplanner.feature.release_notes.generated.resources.release_notes_tab_upcoming
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesTab
import org.jetbrains.compose.resources.StringResource

fun ReleaseNotesTab.toTitleResource(): StringResource = when (this) {
    ReleaseNotesTab.LATEST -> Res.string.release_notes_tab_latest
    ReleaseNotesTab.UPCOMING -> Res.string.release_notes_tab_upcoming
    ReleaseNotesTab.PAST_VERSIONS -> Res.string.release_notes_tab_past
}
