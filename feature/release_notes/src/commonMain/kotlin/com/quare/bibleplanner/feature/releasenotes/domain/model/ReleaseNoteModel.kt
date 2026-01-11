package com.quare.bibleplanner.feature.releasenotes.domain.model

import com.quare.bibleplanner.core.date.DateRepresentation

data class ReleaseNoteModel(
    val version: String,
    val changes: List<String>,
    val dateRepresentation: DateRepresentation? = null,
)
