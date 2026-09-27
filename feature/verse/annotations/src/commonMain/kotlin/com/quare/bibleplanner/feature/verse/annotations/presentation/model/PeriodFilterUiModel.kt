package com.quare.bibleplanner.feature.verse.annotations.presentation.model

internal data class PeriodFilterUiModel(
    val period: AnnotationPeriod,
    val count: Int,
    val isSelected: Boolean,
)
