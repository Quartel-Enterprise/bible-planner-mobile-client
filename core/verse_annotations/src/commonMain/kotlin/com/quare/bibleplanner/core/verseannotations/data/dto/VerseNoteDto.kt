package com.quare.bibleplanner.core.verseannotations.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Remote row of the `verse_notes` table. The passage is payload rather than identity, so editing
 * which verses a note covers keeps the same [id].
 */
@Serializable
internal data class VerseNoteDto(
    @SerialName("user_id") val userId: String,
    @SerialName("id") val id: String,
    @SerialName("book_id") val bookId: String,
    @SerialName("chapter_number") val chapterNumber: Int,
    @SerialName("verse_numbers") val verseNumbers: List<Int>,
    @SerialName("text") val text: String,
    @SerialName("is_deleted") val isDeleted: Boolean,
    @SerialName("updated_at") val updatedAt: String,
)
