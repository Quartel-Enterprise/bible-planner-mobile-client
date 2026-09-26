package com.quare.bibleplanner.feature.daystudy.presentation.model

import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyModel
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.provider.platform.Platform

internal data class DayStudyRouteUiState(
    val card: Loadable<DayStudyCardUiModel>,
    val generation: DayStudyGenerationUiModel?,
    val generationError: DayStudyGenerationError?,
    val openStudy: DayStudyModel?,
    val isOpeningStudy: Boolean,
    val passageLabel: String?,
    val platform: Platform,
)
