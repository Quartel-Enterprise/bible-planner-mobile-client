package com.quare.bibleplanner.core.verseannotations.domain.usecase.impl

import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseNoteRepository
import com.quare.bibleplanner.core.verseannotations.domain.usecase.DeleteVerseNote

internal class DeleteVerseNoteUseCase(
    private val verseNoteRepository: VerseNoteRepository,
) : DeleteVerseNote {
    override suspend fun invoke(noteId: String) {
        verseNoteRepository.delete(noteId)
    }
}
