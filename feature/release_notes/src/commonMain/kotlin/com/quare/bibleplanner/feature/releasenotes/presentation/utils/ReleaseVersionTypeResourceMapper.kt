package com.quare.bibleplanner.feature.releasenotes.presentation.utils

import bibleplanner.feature.release_notes.generated.resources.Res
import bibleplanner.feature.release_notes.generated.resources.release_notes_tag_latest
import bibleplanner.feature.release_notes.generated.resources.release_notes_tag_upcoming
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseVersionType
import org.jetbrains.compose.resources.StringResource

fun ReleaseVersionType.toTagResource(): StringResource? = when (this) {
    ReleaseVersionType.LATEST -> Res.string.release_notes_tag_latest
    ReleaseVersionType.UPCOMING -> Res.string.release_notes_tag_upcoming
    ReleaseVersionType.PAST -> null
}
