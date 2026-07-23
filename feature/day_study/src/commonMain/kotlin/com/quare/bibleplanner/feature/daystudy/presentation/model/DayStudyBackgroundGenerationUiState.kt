package com.quare.bibleplanner.feature.daystudy.presentation.model

import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationJob

internal data class DayStudyBackgroundGenerationUiState(
    val isVisible: Boolean,
    val jobs: List<DayStudyGenerationJob>,
)
