package com.quare.bibleplanner.core.verseannotations.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Remote row of the `saved_verses` table. */
@Serializable
internal data class SavedVerseDto(
    @SerialName("user_id") val userId: String,
    @SerialName("book_id") val bookId: String,
    @SerialName("chapter_number") val chapterNumber: Int,
    @SerialName("verse_number") val verseNumber: Int,
    @SerialName("is_saved") val isSaved: Boolean,
    @SerialName("updated_at") val updatedAt: String,
)
