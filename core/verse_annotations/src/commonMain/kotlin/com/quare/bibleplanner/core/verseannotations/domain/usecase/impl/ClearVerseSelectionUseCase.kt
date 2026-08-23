package com.quare.bibleplanner.core.verseannotations.domain.usecase.impl

import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseSelectionRepository
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ClearVerseSelection

internal class ClearVerseSelectionUseCase(
    private val verseSelectionRepository: VerseSelectionRepository,
) : ClearVerseSelection {
    override fun invoke() {
        verseSelectionRepository.clear()
    }
}
