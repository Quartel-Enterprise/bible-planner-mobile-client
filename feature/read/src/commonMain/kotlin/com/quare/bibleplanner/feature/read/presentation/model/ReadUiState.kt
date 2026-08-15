package com.quare.bibleplanner.feature.read.presentation.model

import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel

data class ReadUiState(
    val header: ReadHeaderUiModel,
    val content: ReadContentUiState,
    val settings: ReaderSettingsModel,
    val selection: ReadSelectionUiModel?,
    val isSidePanelOpen: Boolean,
)
