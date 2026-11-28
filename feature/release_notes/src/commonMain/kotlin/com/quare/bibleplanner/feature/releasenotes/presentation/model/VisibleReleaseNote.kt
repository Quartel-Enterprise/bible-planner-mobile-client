package com.quare.bibleplanner.feature.releasenotes.presentation.model

import com.quare.bibleplanner.feature.releasenotes.domain.model.ReleaseNoteModel

data class VisibleReleaseNote(
    val note: ReleaseNoteModel,
    val type: ReleaseVersionType,
)
