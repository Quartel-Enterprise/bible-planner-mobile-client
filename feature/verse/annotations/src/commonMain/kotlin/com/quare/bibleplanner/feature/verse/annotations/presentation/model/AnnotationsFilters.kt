package com.quare.bibleplanner.feature.verse.annotations.presentation.model

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor

internal data class AnnotationsFilters(
    val type: AnnotationTypeFilter,
    val color: HighlightColor?,
    val bookId: BookId?,
    val period: AnnotationPeriod,
    val customRange: AnnotationDateRange?,
    val query: String,
)
