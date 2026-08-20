package com.quare.bibleplanner.core.verseannotations.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Remote row of the `verse_highlights` table. A null [color] is the tombstone of a removed
 * highlight: the sync engine never deletes rows, so the removal travels as a payload.
 */
@Serializable
internal data class VerseHighlightDto(
    @SerialName("user_id") val userId: String,
    @SerialName("bible_version_id") val bibleVersionId: String,
    @SerialName("book_id") val bookId: String,
    @SerialName("chapter_number") val chapterNumber: Int,
    @SerialName("verse_number") val verseNumber: Int,
    @SerialName("color") val color: String?,
    @SerialName("updated_at") val updatedAt: String,
)
