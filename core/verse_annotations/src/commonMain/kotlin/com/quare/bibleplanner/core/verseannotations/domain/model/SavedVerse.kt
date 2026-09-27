package com.quare.bibleplanner.core.verseannotations.domain.model

data class SavedVerse(
    val ref: VerseRef,
    val updatedAtEpochMillis: Long,
)
