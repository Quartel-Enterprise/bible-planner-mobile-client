package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.books.data.dto.VerseReadDto
import com.quare.bibleplanner.core.provider.room.relation.PendingVerseRead
import kotlin.time.Instant

internal class VerseReadMapper {
    /**
     * Maps a pending verse read projection to the remote payload. [PendingVerseRead.readUpdatedAt] is
     * assumed non-null: callers only push rows flagged pending, which always have a timestamp.
     */
    fun toDto(
        userId: String,
        entity: PendingVerseRead,
    ): VerseReadDto = VerseReadDto(
        userId = userId,
        bookId = entity.bookId,
        chapterNumber = entity.chapterNumber,
        verseNumber = entity.verseNumber,
        isRead = entity.isRead,
        updatedAt = Instant.fromEpochMilliseconds(entity.readUpdatedAt ?: 0L).toString(),
    )

    fun toEpochMillis(updatedAt: String): Long = Instant.parse(updatedAt).toEpochMilliseconds()
}
