package com.quare.bibleplanner.core.verseannotations.domain.usecase.impl

import com.quare.bibleplanner.core.verseannotations.domain.model.AnnotatedPassage
import com.quare.bibleplanner.core.verseannotations.domain.repository.SavedVerseRepository
import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseHighlightRepository
import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseNoteRepository
import com.quare.bibleplanner.core.verseannotations.domain.usecase.RemovePassageAnnotations

internal class RemovePassageAnnotationsUseCase(
    private val verseHighlightRepository: VerseHighlightRepository,
    private val savedVerseRepository: SavedVerseRepository,
    private val verseNoteRepository: VerseNoteRepository,
) : RemovePassageAnnotations {
    override suspend fun invoke(passage: AnnotatedPassage) {
        if (passage.highlightColor != null) {
            verseHighlightRepository.setColor(
                refs = passage.refs,
                color = null,
            )
        }
        if (passage.isSaved) {
            savedVerseRepository.setSaved(
                refs = passage.refs,
                isSaved = false,
            )
        }
        passage.note?.let { note -> verseNoteRepository.delete(note.id) }
    }
}
