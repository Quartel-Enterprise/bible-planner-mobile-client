package com.quare.bibleplanner.core.verseannotations.domain.model

internal data class VerseMark(
    val color: HighlightColor?,
    val isSaved: Boolean,
    val updatedAtEpochMillis: Long,
)
