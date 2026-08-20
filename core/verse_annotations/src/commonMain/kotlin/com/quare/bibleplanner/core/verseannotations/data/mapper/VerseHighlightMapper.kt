package com.quare.bibleplanner.core.verseannotations.data.mapper

import com.quare.bibleplanner.core.provider.room.entity.VerseHighlightEntity
import com.quare.bibleplanner.core.verseannotations.data.dto.VerseHighlightDto

internal class VerseHighlightMapper(
    private val syncTimestampMapper: SyncTimestampMapper,
) {
    fun toDto(
        userId: String,
        entity: VerseHighlightEntity,
    ): VerseHighlightDto = VerseHighlightDto(
        userId = userId,
        bibleVersionId = entity.bibleVersionId,
        bookId = entity.bookId,
        chapterNumber = entity.chapterNumber,
        verseNumber = entity.verseNumber,
        color = entity.color,
        updatedAt = syncTimestampMapper.toIso(entity.updatedAtEpochMillis),
    )

    fun toEntity(dto: VerseHighlightDto): VerseHighlightEntity = VerseHighlightEntity(
        bibleVersionId = dto.bibleVersionId,
        bookId = dto.bookId,
        chapterNumber = dto.chapterNumber,
        verseNumber = dto.verseNumber,
        color = dto.color,
        updatedAtEpochMillis = syncTimestampMapper.toEpochMillis(dto.updatedAt),
        isPendingSync = false,
    )
}
