package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.books.data.dto.ChapterReadDto
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import kotlin.time.Instant

internal class ChapterReadMapper {
    // Why: only pending rows are pushed and they always carry readUpdatedAt, so the 0L fallback
    // never reaches the backend.
    fun toDto(
        userId: String,
        entity: ChapterEntity,
    ): ChapterReadDto = ChapterReadDto(
        userId = userId,
        bookId = entity.bookId,
        chapterNumber = entity.number,
        isRead = entity.isRead,
        updatedAt = Instant.fromEpochMilliseconds(entity.readUpdatedAt ?: 0L).toString(),
    )

    fun toEpochMillis(updatedAt: String): Long = Instant.parse(updatedAt).toEpochMilliseconds()
}
