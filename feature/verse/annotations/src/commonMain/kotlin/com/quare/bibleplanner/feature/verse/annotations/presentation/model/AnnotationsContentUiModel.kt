package com.quare.bibleplanner.feature.verse.annotations.presentation.model

import com.quare.bibleplanner.core.model.book.BookId
import kotlinx.datetime.LocalDate

internal data class AnnotationsContentUiModel(
    val totalCount: Int,
    val shownCount: Int,
    val typeFilters: List<TypeFilterUiModel>,
    val colorFilters: List<ColorFilterUiModel>,
    val bookFilters: List<BookFilterUiModel>,
    val periodFilters: List<PeriodFilterUiModel>,
    val selectedBookId: BookId?,
    val selectedPeriod: AnnotationPeriod,
    val customRange: AnnotationDateRange?,
    val today: LocalDate,
    val hasActiveFilters: Boolean,
    val groups: List<AnnotationGroupUiModel>,
)
