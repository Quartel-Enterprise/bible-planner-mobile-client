package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.books.data.dto.ChapterReadDto
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import kotlin.time.Instant

internal class ChapterReadMapper {
    /**
     * Maps a local chapter row to the remote read payload. [ChapterEntity.readUpdatedAt] is assumed
     * non-null: callers only push rows flagged pending, which always have a timestamp.
     */
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
