package com.quare.bibleplanner.core.verseannotations.domain.usecase.impl

import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseHighlightRepository
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ApplyHighlightColor

internal class ApplyHighlightColorUseCase(
    private val verseHighlightRepository: VerseHighlightRepository,
) : ApplyHighlightColor {
    override suspend fun invoke(
        refs: List<VerseRef>,
        color: HighlightColor,
    ): Boolean {
        val currentColors = verseHighlightRepository.getColors(refs)
        val isAlreadyApplied = refs.isNotEmpty() && refs.all { ref -> currentColors[ref]?.key == color.key }
        verseHighlightRepository.setColor(
            refs = refs,
            color = color.takeUnless { isAlreadyApplied },
        )
        return !isAlreadyApplied
    }
}
