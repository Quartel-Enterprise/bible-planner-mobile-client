package com.quare.bibleplanner.feature.daystudy.presentation.model

import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyModel

internal data class DayStudyRouteUiState(
    val card: Loadable<DayStudyCardUiModel>,
    val generation: DayStudyGenerationUiModel?,
    val generationError: DayStudyGenerationError?,
    val openStudy: DayStudyModel?,
    val isOpeningStudy: Boolean,
    val passageLabel: String?,
    val platform: Platform,
)
