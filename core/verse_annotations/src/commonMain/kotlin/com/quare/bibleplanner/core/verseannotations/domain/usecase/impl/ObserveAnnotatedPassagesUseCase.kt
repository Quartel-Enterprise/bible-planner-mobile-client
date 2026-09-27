package com.quare.bibleplanner.core.verseannotations.domain.usecase.impl

import com.quare.bibleplanner.core.verseannotations.domain.factory.AnnotatedPassageFactory
import com.quare.bibleplanner.core.verseannotations.domain.model.AnnotatedPassage
import com.quare.bibleplanner.core.verseannotations.domain.repository.SavedVerseRepository
import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseHighlightRepository
import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseNoteRepository
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ObserveAnnotatedPassages
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

internal class ObserveAnnotatedPassagesUseCase(
    private val verseHighlightRepository: VerseHighlightRepository,
    private val savedVerseRepository: SavedVerseRepository,
    private val verseNoteRepository: VerseNoteRepository,
    private val annotatedPassageFactory: AnnotatedPassageFactory,
) : ObserveAnnotatedPassages {
    override fun invoke(bibleVersionId: String): Flow<List<AnnotatedPassage>> = combine(
        verseHighlightRepository.observeVersionHighlights(bibleVersionId),
        savedVerseRepository.observeVersionSavedVerses(bibleVersionId),
        verseNoteRepository.observeVersionNotes(bibleVersionId),
        annotatedPassageFactory::create,
    )
}
