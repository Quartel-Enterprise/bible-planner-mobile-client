package com.quare.bibleplanner.feature.daystudy.presentation.model

import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyModel

internal data class DayStudyUiState(
    val card: Loadable<DayStudyCardUiModel>,
    val generation: DayStudyGenerationUiModel?,
    val isStudyOpen: Boolean,
    val openStudy: DayStudyModel?,
)
