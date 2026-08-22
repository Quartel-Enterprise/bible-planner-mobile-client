package com.quare.bibleplanner.core.verseannotations.data.mapper

import com.quare.bibleplanner.core.provider.room.entity.SavedVerseEntity
import com.quare.bibleplanner.core.verseannotations.data.dto.SavedVerseDto

internal class SavedVerseMapper(
    private val syncTimestampMapper: SyncTimestampMapper,
) {
    fun toDto(
        userId: String,
        entity: SavedVerseEntity,
    ): SavedVerseDto = SavedVerseDto(
        userId = userId,
        bibleVersionId = entity.bibleVersionId,
        bookId = entity.bookId,
        chapterNumber = entity.chapterNumber,
        verseNumber = entity.verseNumber,
        isSaved = entity.isSaved,
        updatedAt = syncTimestampMapper.toIso(entity.updatedAtEpochMillis),
    )

    fun toEntity(dto: SavedVerseDto): SavedVerseEntity = SavedVerseEntity(
        bibleVersionId = dto.bibleVersionId,
        bookId = dto.bookId,
        chapterNumber = dto.chapterNumber,
        verseNumber = dto.verseNumber,
        isSaved = dto.isSaved,
        updatedAtEpochMillis = syncTimestampMapper.toEpochMillis(dto.updatedAt),
        isPendingSync = false,
    )
}
