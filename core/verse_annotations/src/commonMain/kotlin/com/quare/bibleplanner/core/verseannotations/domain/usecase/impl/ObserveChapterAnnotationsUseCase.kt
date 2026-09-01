package com.quare.bibleplanner.core.verseannotations.domain.usecase.impl

import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.ChapterAnnotations
import com.quare.bibleplanner.core.verseannotations.domain.repository.SavedVerseRepository
import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseHighlightRepository
import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseNoteRepository
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ObserveChapterAnnotations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

internal class ObserveChapterAnnotationsUseCase(
    private val verseHighlightRepository: VerseHighlightRepository,
    private val savedVerseRepository: SavedVerseRepository,
    private val verseNoteRepository: VerseNoteRepository,
) : ObserveChapterAnnotations {
    override fun invoke(chapter: ChapterRef): Flow<ChapterAnnotations> = combine(
        verseHighlightRepository.observeChapterHighlights(chapter),
        savedVerseRepository.observeChapterSavedVerses(chapter),
        verseNoteRepository.observeChapterNotes(chapter),
    ) { highlights, savedVerses, notes ->
        ChapterAnnotations(
            highlightColorByVerse = highlights,
            savedVerseNumbers = savedVerses,
            noteIdByVerse = notes.flatMap { note -> note.verseNumbers.map { it to note.id } }.toMap(),
        )
    }
}
