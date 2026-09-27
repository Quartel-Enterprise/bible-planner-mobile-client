package com.quare.bibleplanner.feature.verse.annotations.presentation.model

import com.quare.bibleplanner.feature.verse.annotations.domain.model.AnnotationEntry
import kotlinx.datetime.LocalDate

internal data class DatedAnnotationEntry(
    val entry: AnnotationEntry,
    val date: LocalDate,
    val daysAgo: Int,
)
