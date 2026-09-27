package com.quare.bibleplanner.feature.verse.annotations.presentation.model

import kotlinx.datetime.Month

internal sealed interface AnnotationGroupLabel {
    data object Today : AnnotationGroupLabel

    data object Yesterday : AnnotationGroupLabel

    data class MonthOfYear(
        val month: Month,
        val year: Int?,
    ) : AnnotationGroupLabel
}
