package com.quare.bibleplanner.feature.verse.annotations.presentation.model

import kotlinx.datetime.LocalDate

internal data class AnnotationDateRange(
    val start: LocalDate,
    val end: LocalDate,
)
