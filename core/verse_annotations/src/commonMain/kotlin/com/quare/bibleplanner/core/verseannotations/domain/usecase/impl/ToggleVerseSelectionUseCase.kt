package com.quare.bibleplanner.core.verseannotations.domain.usecase.impl

import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseSelection
import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseSelectionRepository
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ToggleVerseSelection

internal class ToggleVerseSelectionUseCase(
    private val verseSelectionRepository: VerseSelectionRepository,
) : ToggleVerseSelection {
    override fun invoke(
        chapter: ChapterRef,
        verseNumber: Int,
    ): VerseSelection? = verseSelectionRepository.toggle(
        chapter = chapter,
        verseNumber = verseNumber,
    )
}
