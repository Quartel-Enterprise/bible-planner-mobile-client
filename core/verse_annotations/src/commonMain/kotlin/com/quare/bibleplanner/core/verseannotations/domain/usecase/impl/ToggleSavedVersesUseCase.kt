package com.quare.bibleplanner.core.verseannotations.domain.usecase.impl

import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import com.quare.bibleplanner.core.verseannotations.domain.repository.SavedVerseRepository
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ToggleSavedVerses

internal class ToggleSavedVersesUseCase(
    private val savedVerseRepository: SavedVerseRepository,
) : ToggleSavedVerses {
    override suspend fun invoke(refs: List<VerseRef>): Boolean {
        val isSaved = !savedVerseRepository.areAllSaved(refs)
        savedVerseRepository.setSaved(
            refs = refs,
            isSaved = isSaved,
        )
        return isSaved
    }
}
