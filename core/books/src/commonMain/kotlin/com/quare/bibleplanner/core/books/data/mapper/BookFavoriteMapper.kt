package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.books.data.dto.BookFavoriteDto
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import kotlin.time.Instant

internal class BookFavoriteMapper {
    /**
     * Maps a local book row to the remote favorite payload. [BookEntity.favoriteUpdatedAt] is assumed non-null:
     * callers only push rows flagged pending, which always have a timestamp.
     */
    fun toDto(
        userId: String,
        entity: BookEntity,
    ): BookFavoriteDto = BookFavoriteDto(
        userId = userId,
        bookId = entity.id,
        isFavorite = entity.isFavorite,
        updatedAt = Instant.fromEpochMilliseconds(entity.favoriteUpdatedAt ?: 0L).toString(),
    )

    fun toEpochMillis(updatedAt: String): Long = Instant.parse(updatedAt).toEpochMilliseconds()
}
