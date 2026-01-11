package com.quare.bibleplanner.feature.themeselection.presentation.model

import com.quare.bibleplanner.ui.theme.model.ContrastType

data class ThemeSelectionUiState(
    val isMaterialYouToggleOn: Boolean?,
    val options: List<ThemeSelectionModel>,
    val selectedContrast: ContrastType = ContrastType.Standard,
)
