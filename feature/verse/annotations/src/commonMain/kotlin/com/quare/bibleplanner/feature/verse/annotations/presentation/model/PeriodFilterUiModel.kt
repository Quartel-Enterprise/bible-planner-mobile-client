package com.quare.bibleplanner.feature.verse.annotations.presentation.model

data class PeriodFilterUiModel(
    val period: AnnotationPeriod,
    val count: Int,
    val isSelected: Boolean,
)
