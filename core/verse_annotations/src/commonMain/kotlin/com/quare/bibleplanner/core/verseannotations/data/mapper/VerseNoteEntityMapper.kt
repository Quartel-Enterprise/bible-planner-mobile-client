package com.quare.bibleplanner.core.verseannotations.data.mapper

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.provider.room.entity.VerseNoteEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseNoteVerseEntity
import com.quare.bibleplanner.core.provider.room.relation.VerseNoteWithVerses
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseNote

internal class VerseNoteEntityMapper {
    fun toDomain(relation: VerseNoteWithVerses): VerseNote = VerseNote(
        id = relation.note.id,
        chapter = ChapterRef(
            bibleVersionId = relation.note.bibleVersionId,
            bookId = BookId.valueOf(relation.note.bookId),
            chapterNumber = relation.note.chapterNumber,
        ),
        verseNumbers = relation.verses.sortedBy { it.position }.map { it.verseNumber },
        text = relation.note.text,
        createdAtEpochMillis = relation.note.createdAtEpochMillis,
        updatedAtEpochMillis = relation.note.updatedAtEpochMillis,
    )

    fun toEntity(
        note: VerseNote,
        isPendingSync: Boolean,
    ): VerseNoteEntity = VerseNoteEntity(
        id = note.id,
        bibleVersionId = note.chapter.bibleVersionId,
        bookId = note.chapter.bookId.name,
        chapterNumber = note.chapter.chapterNumber,
        text = note.text,
        isDeleted = false,
        createdAtEpochMillis = note.createdAtEpochMillis,
        updatedAtEpochMillis = note.updatedAtEpochMillis,
        isPendingSync = isPendingSync,
    )

    fun toVerseEntities(note: VerseNote): List<VerseNoteVerseEntity> = note.verseNumbers
        .mapIndexed { index, verseNumber ->
            VerseNoteVerseEntity(
                noteId = note.id,
                verseNumber = verseNumber,
                position = index,
            )
        }
}
