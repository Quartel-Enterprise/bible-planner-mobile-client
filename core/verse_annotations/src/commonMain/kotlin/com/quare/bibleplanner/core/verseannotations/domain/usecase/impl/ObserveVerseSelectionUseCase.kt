package com.quare.bibleplanner.core.verseannotations.domain.usecase.impl

import com.quare.bibleplanner.core.verseannotations.domain.model.VerseSelection
import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseSelectionRepository
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ObserveVerseSelection
import kotlinx.coroutines.flow.StateFlow

internal class ObserveVerseSelectionUseCase(
    private val verseSelectionRepository: VerseSelectionRepository,
) : ObserveVerseSelection {
    override fun invoke(): StateFlow<VerseSelection?> = verseSelectionRepository.selection
}
