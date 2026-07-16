package com.quare.bibleplanner.feature.daystudy.presentation.model

import com.quare.bibleplanner.core.model.loadable.Loadable

internal data class DayStudyUiState(
    val card: Loadable<DayStudyCardUiModel>,
    val generation: DayStudyGenerationUiModel?,
    val isOpeningStudy: Boolean,
)
