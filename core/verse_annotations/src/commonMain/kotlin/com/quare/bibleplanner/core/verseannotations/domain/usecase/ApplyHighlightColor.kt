package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef

fun interface ApplyHighlightColor {
    // Why: tapping the active swatch is how the design removes a highlight, so a colour every verse
    // already carries is cleared; returns true when applied, false when cleared.
    suspend operator fun invoke(
        refs: List<VerseRef>,
        color: HighlightColor,
    ): Boolean
}
