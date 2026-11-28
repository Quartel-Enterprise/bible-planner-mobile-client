package com.quare.bibleplanner.feature.verse.annotations.presentation.model

import kotlinx.datetime.LocalDate

data class AnnotationDateRange(
    val start: LocalDate,
    val end: LocalDate,
)
