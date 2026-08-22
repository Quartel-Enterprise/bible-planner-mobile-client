package com.quare.bibleplanner.core.verseannotations.domain.usecase.impl

import com.quare.bibleplanner.core.verseannotations.domain.repository.HighlightPaletteRepository
import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseHighlightRepository
import com.quare.bibleplanner.core.verseannotations.domain.usecase.RemoveCustomHighlightColor

internal class RemoveCustomHighlightColorUseCase(
    private val highlightPaletteRepository: HighlightPaletteRepository,
    private val verseHighlightRepository: VerseHighlightRepository,
) : RemoveCustomHighlightColor {
    override suspend fun invoke(
        colorKey: String,
        shouldKeepHighlights: Boolean,
    ) {
        highlightPaletteRepository.remove(colorKey)
        if (!shouldKeepHighlights) verseHighlightRepository.removeAllWithColor(colorKey)
    }
}
