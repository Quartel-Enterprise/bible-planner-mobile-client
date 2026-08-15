package com.quare.bibleplanner.core.verseannotations.domain.usecase.impl

import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.repository.HighlightPaletteRepository
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ObserveHighlightPalette
import kotlinx.coroutines.flow.Flow

internal class ObserveHighlightPaletteUseCase(
    private val highlightPaletteRepository: HighlightPaletteRepository,
) : ObserveHighlightPalette {
    override fun invoke(): Flow<List<HighlightColor.Custom>> = highlightPaletteRepository.observeCustomColors()
}
