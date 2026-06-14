package com.quare.bibleplanner.core.books.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Remote row of the `verse_reads` table: one user-scoped verse read flag for a verse-range read,
 * keyed by (`user_id`, `book_id`, `chapter_number`, `verse_number`), reconciled by `updated_at`
 * (Last-Write-Wins).
 */
@Serializable
internal data class VerseReadDto(
    @SerialName("user_id") val userId: String,
    @SerialName("book_id") val bookId: String,
    @SerialName("chapter_number") val chapterNumber: Int,
    @SerialName("verse_number") val verseNumber: Int,
    @SerialName("is_read") val isRead: Boolean,
    @SerialName("updated_at") val updatedAt: String,
)
