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
    val verseFocus: VerseFocusUiModel?,
    val isOpeningChapterStudy: Boolean,
    /** On a wide window the chapter study sits beside the text, so the reader offers no way to open it. */
    val isChapterStudyBeside: Boolean,
)
