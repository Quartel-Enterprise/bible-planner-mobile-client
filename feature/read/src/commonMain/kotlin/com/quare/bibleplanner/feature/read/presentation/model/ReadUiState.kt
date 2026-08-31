package com.quare.bibleplanner.feature.read.presentation.model

import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel

data class ReadUiState(
    val header: ReadHeaderUiModel,
    val content: ReadContentUiState,
    val settings: ReaderSettingsModel,
    val isLoadingPreviousChapter: Boolean,
    val isLoadingNextChapter: Boolean,
    val dayCompletionBanner: PlanDayLocationModel?,
)
