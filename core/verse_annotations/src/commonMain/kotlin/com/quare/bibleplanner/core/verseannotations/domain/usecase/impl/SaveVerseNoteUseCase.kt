package com.quare.bibleplanner.core.verseannotations.domain.usecase.impl

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseNote
import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseNoteRepository
import com.quare.bibleplanner.core.verseannotations.domain.usecase.SaveVerseNote
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class SaveVerseNoteUseCase(
    private val verseNoteRepository: VerseNoteRepository,
    private val currentTimestampProvider: CurrentTimestampProvider,
) : SaveVerseNote {
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun invoke(
        noteId: String?,
        chapter: ChapterRef,
        verseNumbers: List<Int>,
        text: String,
    ) {
        val trimmedText = text.trim()
        if (trimmedText.isEmpty()) {
            noteId?.let { verseNoteRepository.delete(it) }
            return
        }
        val now = currentTimestampProvider.getCurrentTimestamp()
        val existingNote = noteId?.let { verseNoteRepository.getNote(it) }
        verseNoteRepository.upsert(
            VerseNote(
                id = existingNote?.id ?: noteId ?: Uuid.random().toString(),
                chapter = chapter,
                verseNumbers = verseNumbers,
                text = trimmedText,
                createdAtEpochMillis = existingNote?.createdAtEpochMillis ?: now,
                updatedAtEpochMillis = now,
            ),
        )
    }
}
