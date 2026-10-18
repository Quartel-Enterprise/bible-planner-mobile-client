package com.quare.bibleplanner.feature.daystudy.presentation.model

import software.quare.loadable.Loadable

internal data class DayStudyUiState(
    val card: Loadable<DayStudyCardUiModel>,
    val generation: DayStudyGenerationUiModel?,
    val isOpeningStudy: Boolean,
)
