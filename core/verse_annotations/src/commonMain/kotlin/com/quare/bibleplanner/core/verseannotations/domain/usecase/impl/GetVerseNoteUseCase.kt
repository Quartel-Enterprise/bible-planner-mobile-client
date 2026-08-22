package com.quare.bibleplanner.core.verseannotations.domain.usecase.impl

import com.quare.bibleplanner.core.verseannotations.domain.model.VerseNote
import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseNoteRepository
import com.quare.bibleplanner.core.verseannotations.domain.usecase.GetVerseNote

internal class GetVerseNoteUseCase(
    private val verseNoteRepository: VerseNoteRepository,
) : GetVerseNote {
    override suspend fun invoke(noteId: String): VerseNote? = verseNoteRepository.getNote(noteId)
}
