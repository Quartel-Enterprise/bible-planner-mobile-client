package com.quare.bibleplanner.core.verseannotations.domain.usecase.impl

import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.repository.HighlightPaletteRepository
import com.quare.bibleplanner.core.verseannotations.domain.usecase.AddCustomHighlightColor

internal class AddCustomHighlightColorUseCase(
    private val highlightPaletteRepository: HighlightPaletteRepository,
) : AddCustomHighlightColor {
    override suspend fun invoke(color: HighlightColor.Custom) {
        highlightPaletteRepository.add(color)
    }
}
