package com.quare.bibleplanner.core.verseannotations.domain.model

data class VerseHighlight(
    val ref: VerseRef,
    val color: HighlightColor,
    val updatedAtEpochMillis: Long,
)
