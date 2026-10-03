package com.quare.bibleplanner.core.verseannotations.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Why: the passage is payload, not identity, so editing a note's verses keeps the same id.
@Serializable
internal data class VerseNoteDto(
    @SerialName("user_id") val userId: String,
    @SerialName("id") val id: String,
    @SerialName("bible_version_id") val bibleVersionId: String,
    @SerialName("book_id") val bookId: String,
    @SerialName("chapter_number") val chapterNumber: Int,
    @SerialName("verse_numbers") val verseNumbers: List<Int>,
    @SerialName("text") val text: String,
    @SerialName("is_deleted") val isDeleted: Boolean,
    @SerialName("updated_at") val updatedAt: String,
)
