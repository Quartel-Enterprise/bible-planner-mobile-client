package com.quare.bibleplanner.core.books.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Remote row of the `chapter_reads` table: one user-scoped chapter read flag, keyed by
 * (`user_id`, `book_id`, `chapter_number`), reconciled by `updated_at` (Last-Write-Wins).
 */
@Serializable
internal data class ChapterReadDto(
    @SerialName("user_id") val userId: String,
    @SerialName("book_id") val bookId: String,
    @SerialName("chapter_number") val chapterNumber: Int,
    @SerialName("is_read") val isRead: Boolean,
    @SerialName("updated_at") val updatedAt: String,
)
