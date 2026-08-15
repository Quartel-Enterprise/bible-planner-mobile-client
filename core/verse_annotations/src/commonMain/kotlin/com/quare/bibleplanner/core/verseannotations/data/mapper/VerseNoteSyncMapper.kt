package com.quare.bibleplanner.core.verseannotations.data.mapper

import com.quare.bibleplanner.core.provider.room.entity.VerseNoteEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseNoteVerseEntity
import com.quare.bibleplanner.core.provider.room.relation.VerseNoteWithVerses
import com.quare.bibleplanner.core.verseannotations.data.dto.VerseNoteDto

internal class VerseNoteSyncMapper(
    private val syncTimestampMapper: SyncTimestampMapper,
) {
    fun toDto(
        userId: String,
        relation: VerseNoteWithVerses,
    ): VerseNoteDto = VerseNoteDto(
        userId = userId,
        id = relation.note.id,
        bookId = relation.note.bookId,
        chapterNumber = relation.note.chapterNumber,
        verseNumbers = relation.verses.sortedBy { it.position }.map { it.verseNumber },
        text = relation.note.text,
        isDeleted = relation.note.isDeleted,
        updatedAt = syncTimestampMapper.toIso(relation.note.updatedAtEpochMillis),
    )

    /**
     * The remote row carries no creation stamp, so a note this device has never seen is created with
     * its update time: it only orders notes within a chapter, and the backend's own ordering is the
     * same one the originating device produced.
     */
    fun toEntity(dto: VerseNoteDto): VerseNoteEntity {
        val updatedAt = syncTimestampMapper.toEpochMillis(dto.updatedAt)
        return VerseNoteEntity(
            id = dto.id,
            bookId = dto.bookId,
            chapterNumber = dto.chapterNumber,
            text = dto.text,
            isDeleted = dto.isDeleted,
            createdAtEpochMillis = updatedAt,
            updatedAtEpochMillis = updatedAt,
            isPendingSync = false,
        )
    }

    fun toVerseEntities(dto: VerseNoteDto): List<VerseNoteVerseEntity> = dto.verseNumbers
        .mapIndexed { index, verseNumber ->
            VerseNoteVerseEntity(
                noteId = dto.id,
                verseNumber = verseNumber,
                position = index,
            )
        }
}
