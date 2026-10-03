package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.books.data.dto.BookFavoriteDto
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import kotlin.time.Instant

internal class BookFavoriteMapper {
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
