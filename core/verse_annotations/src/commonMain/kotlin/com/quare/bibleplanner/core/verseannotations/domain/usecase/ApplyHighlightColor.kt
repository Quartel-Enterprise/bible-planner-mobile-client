package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef

fun interface ApplyHighlightColor {
    suspend operator fun invoke(
        refs: List<VerseRef>,
        color: HighlightColor,
    ): Boolean
}
