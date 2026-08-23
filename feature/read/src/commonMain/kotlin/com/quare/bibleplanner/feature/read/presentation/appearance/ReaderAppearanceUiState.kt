package com.quare.bibleplanner.feature.read.presentation.appearance

import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel

data class ReaderAppearanceUiState(
    val settings: ReaderSettingsModel,
    val isFontMenuExpanded: Boolean,
)
