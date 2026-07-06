package com.quare.bibleplanner.feature.daystudy.presentation.model

internal data class DayStudyCardUiModel(
    val mode: DayStudyCardMode,
    val remainingFree: Int,
    val freeLimit: Int,
    val isPro: Boolean,
)
