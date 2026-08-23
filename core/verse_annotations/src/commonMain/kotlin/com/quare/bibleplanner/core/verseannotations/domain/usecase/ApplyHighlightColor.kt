package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef

fun interface ApplyHighlightColor {
    /**
     * Applies [color] to [refs], or clears the highlight when every verse already carries exactly
     * that colour — tapping the active swatch is how the design removes a highlight.
     *
     * @return true when the colour was applied, false when it was cleared.
     */
    suspend operator fun invoke(
        refs: List<VerseRef>,
        color: HighlightColor,
    ): Boolean
}
